from django.db import models
from django.utils import timezone
from users.models import User

class Playlist(models.Model):
    name = models.CharField(max_length=255)
    owner = models.ForeignKey(User, on_delete=models.CASCADE, related_name='playlists')
    tracks = models.JSONField(default=list)  # Stores array of track IDs
    collaborators = models.ManyToManyField(User, related_name='collaborative_playlists', blank=True)
    is_public = models.BooleanField(default=True)
    followers = models.ManyToManyField(User, related_name='followed_playlists', blank=True)
    could_edit = models.JSONField(default=list) #if the playlist is public, this will be empty
    date = models.DateField(default=timezone.now)

    class Meta:
        db_table = 'playlists'
        ordering = ['-date']
        indexes = [
            models.Index(fields=['owner']),
            models.Index(fields=['is_public']),
        ]

    def __str__(self):
        return f"{self.name} by {self.owner.name}"
    
    def add_track(self, track_id):
        if track_id not in self.tracks:
            self.tracks.append(track_id)
            self.save()
    
    def remove_track(self, track_id):
        if track_id in self.tracks:
            self.tracks.remove(track_id)
            self.save()
            
    def add_collaborator(self, user):
        if user not in self.collaborators.all():
            self.collaborators.add(user)
            self.save()
            
    def add_follower(self, user):
        if user not in self.followers.all():
            self.followers.add(user)
            self.save()
            
    def remove_follower(self, user):
        if user in self.followers.all():
            self.followers.remove(user)
            self.save()
            
    def set_status(self, is_public):
        self.is_public = is_public
        self.save()
    
    @property
    def track_count(self):
        return len(self.tracks)
    
    @property
    def get_followers(self):
        return self.followers.all()
    
    @property
    def get_collaborators(self):
        return self.collaborators.all()
    
    @property
    def get_tracks(self):
        return self.tracks
    
    @property
    def get_owner(self):
        return self.owner 
    
    @property
    def who_can_edit(self):
        return self.could_edit