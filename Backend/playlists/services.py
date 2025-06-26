from .models import Playlist
from users.models import User

def get_user_playlist_stats(user):
    """Get user's playlist statistics"""
    try:
        owned_playlists = Playlist.objects.filter(owner=user).count()
        followed_playlists = Playlist.objects.filter(followers=user).count()
        total_tracks = sum(len(p.tracks) for p in Playlist.objects.filter(owner=user))
        
        return {
            'owned_playlists': owned_playlists,
            'followed_playlists': followed_playlists,
            'total_tracks': total_tracks,
        }
    except Exception as e:
        return {'error': str(e)}

def get_popular_playlists(limit=10):
    """Get most followed playlists"""
    try:
        playlists = Playlist.objects.filter(is_public=True).order_by('-followers__count')[:limit]
        return playlists
    except Exception as e:
        return []

def search_playlists(query, user=None):
    """Search playlists by name"""
    try:
        playlists = Playlist.objects.filter(
            name__icontains=query,
            is_public=True
        )
        
        if user and user.is_authenticated:
            # Also include user's private playlists
            user_playlists = Playlist.objects.filter(
                name__icontains=query,
                owner=user
            )
            playlists = playlists.union(user_playlists)
        
        return playlists[:20]
    except Exception as e:
        return []