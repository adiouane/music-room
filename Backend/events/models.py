from django.db import models
from django.utils import timezone
from users.models import User

# Create your models here.
class Events(models.Model):
    title = models.CharField(max_length=255)
    organizer = models.ForeignKey(User, on_delete=models.CASCADE, related_name='organized_events')
    attendees = models.ManyToManyField(User, related_name='attended_events', blank=True)
    description = models.TextField(blank=True, null=True)    
    location = models.CharField(max_length=255)
    image_url = models.URLField(blank=True, null=True)
    songs = models.JSONField(default=list)  # Stores array of song IDs
    managers = models.JSONField(default=list)  # Stores array of manager IDs
    track_votes = models.JSONField(default=dict)  # Stores track votes: {"track_id": ["user_id1", "user_id2"]}
    # New field for user roles mapping
    user_roles = models.JSONField(default=dict)  # {"user_id": "role"} - roles: "owner", "editor", "listener"
    is_public = models.BooleanField(default=True)
    event_start_time = models.DateTimeField(default=timezone.now)
    event_end_time = models.DateTimeField(null=True, blank=True)
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    
    class Meta:
        db_table = 'events'
        ordering = ['-created_at']
        indexes = [
            models.Index(fields=['organizer']),
            models.Index(fields=['is_public']),
            models.Index(fields=['event_start_time']),
        ]

    def __str__(self):
        return self.title
    
    def add_attendee(self, user):
        """Add user to event attendees with 'listener' role"""
        if user not in self.attendees.all():
            self.attendees.add(user)
            # Assign 'listener' role when joining
            self.assign_role(user.id, 'listener')
            return True
        return False
    
    def remove_attendee(self, user):
        """Remove user from event attendees and their role"""
        if user in self.attendees.all():
            self.attendees.remove(user)
            # Remove user role when leaving
            self.remove_user_role(user.id)
            return True
        return False
    
    def assign_role(self, user_id, role):
        """Assign role to user. Roles: 'owner', 'editor', 'listener'"""
        valid_roles = ['owner', 'editor', 'listener']
        if role not in valid_roles:
            return False
        
        user_id = str(user_id)
        self.user_roles[user_id] = role
        self.save()
        return True
    
    def get_user_role(self, user_id):
        """Get user's role in the event"""
        user_id = str(user_id)
        return self.user_roles.get(user_id, None)
    
    def remove_user_role(self, user_id):
        """Remove user's role from the event"""
        user_id = str(user_id)
        if user_id in self.user_roles:
            del self.user_roles[user_id]
            self.save()
            return True
        return False
    
    def assign_editor_role(self, user_id):
        """Assign 'editor' role to user"""
        return self.assign_role(user_id, 'editor')
    
    def transfer_ownership(self, new_owner_id, current_owner_id):
        """Transfer ownership to another user, current owner becomes editor"""
        new_owner_id = str(new_owner_id)
        current_owner_id = str(current_owner_id)
        
        # Check if current user is actually the owner
        if self.get_user_role(current_owner_id) != 'owner':
            return False
        
        # Assign new owner
        self.assign_role(new_owner_id, 'owner')
        # Make previous owner an editor
        self.assign_role(current_owner_id, 'editor')
        
        # Update the organizer field to reflect the new owner
        try:
            new_owner = User.objects.get(id=int(new_owner_id))
            self.organizer = new_owner
            self.save()
            return True
        except User.DoesNotExist:
            return False
    
    def get_users_by_role(self, role):
        """Get list of user IDs with specific role"""
        return [user_id for user_id, user_role in self.user_roles.items() if user_role == role]
    
    def get_all_roles(self):
        """Get all user roles in the event"""
        return self.user_roles
    
    def has_permission(self, user_id, action):
        """Check if user has permission for specific action"""
        user_role = self.get_user_role(user_id)
        
        if not user_role:
            return False
        
        permissions = {
            'owner': ['edit_event', 'delete_event', 'add_tracks', 'remove_tracks', 'manage_users', 'transfer_ownership'],
            'editor': ['add_tracks', 'remove_tracks', 'edit_event'],
            'listener': ['vote_tracks']
        }
        
        return action in permissions.get(user_role, [])
    
    def add_track(self, track_id):
        """Add track to event"""
        if str(track_id) not in self.songs:
            self.songs.append(str(track_id))
            self.save()
            return True
        return False
    
    def remove_track(self, track_id):
        """Remove track from event"""
        if str(track_id) in self.songs:
            self.songs.remove(str(track_id))
            # Also remove votes for this track
            if str(track_id) in self.track_votes:
                del self.track_votes[str(track_id)]
            self.save()
            return True
        return False
    
    def vote_track(self, user_id, track_id):
        """Vote for a track"""
        track_id = str(track_id)
        user_id = str(user_id)
        
        if track_id not in self.songs:
            return False
        
        if track_id not in self.track_votes:
            self.track_votes[track_id] = []
        
        if user_id not in self.track_votes[track_id]:
            self.track_votes[track_id].append(user_id)
            self.save()
            return True
        return False
    
    def unvote_track(self, user_id, track_id):
        """Remove vote for a track"""
        track_id = str(track_id)
        user_id = str(user_id)
        
        if track_id in self.track_votes and user_id in self.track_votes[track_id]:
            self.track_votes[track_id].remove(user_id)
            self.save()
            return True
        return False
    
    def get_track_votes(self, track_id):
        """Get vote count for a track"""
        track_id = str(track_id)
        return len(self.track_votes.get(track_id, []))
    
    def toggle_visibility(self):
        """Toggle event visibility"""
        self.is_public = not self.is_public
        self.save()
    
    @property
    def attendee_count(self):
        return self.attendees.count()
    
    @property
    def track_count(self):
        return len(self.songs)