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
        """Add user to event attendees"""
        if user not in self.attendees.all():
            self.attendees.add(user)
            return True
        return False
    
    def remove_attendee(self, user):
        """Remove user from event attendees"""
        if user in self.attendees.all():
            self.attendees.remove(user)
            return True
        return False
    
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