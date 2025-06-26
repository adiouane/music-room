from django.db import models
from django.contrib.auth.models import AbstractUser

# Create your models here.
class User(models.Model):  # Capitalized class name
    # Required fields for account creation
    name = models.CharField(max_length=255)
    email = models.EmailField(max_length=255, unique=True)
    password = models.CharField(max_length=255)
    
    # Optional fields - can be null/empty by default
    avatar = models.CharField(max_length=255, default='default_avatar.png')
    liked_artists = models.JSONField(default=list)  # Empty list by default
    liked_albums = models.JSONField(default=list)   # Empty list by default
    liked_songs = models.JSONField(default=list)    # Empty list by default
    genres = models.JSONField(default=list)         # Empty list by default
    last_song_played = models.CharField(max_length=255, null=True, blank=True)
    events = models.JSONField(default=list)         # Empty list by default
    friends = models.JSONField(default=list)        # Empty list by default
    
    # Optional profile fields
    date_of_birth = models.DateField(null=True, blank=True)
    
    # Auto-generated fields
    created_at = models.DateTimeField(auto_now_add=True)
    updated_at = models.DateTimeField(auto_now=True)
    last_login = models.DateTimeField(null=True, blank=True)
    
    # Default boolean fields
    is_subscribed = models.BooleanField(default=False)
    is_verified = models.BooleanField(default=False)
    is_active = models.BooleanField(default=True)
    subscription_type = models.CharField(
        max_length=20, 
        choices=[('free', 'Free'), ('premium', 'Premium')], 
        default='free'
    )
    
    class Meta:
        db_table = 'users'
        indexes = [
            models.Index(fields=['email']),
            models.Index(fields=['created_at']),
        ]

    def __str__(self):
        return self.name


