from django.shortcuts import render, get_object_or_404
from django.http import JsonResponse
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi

from .models import Playlist
from users.models import User
from music.services import get_playlist_songs

def check_playlist_permission(request, playlist, action='view'):
    """Check playlist permissions"""
    if not request.user.is_authenticated:
        return False
    
    if action == 'view':
        return (playlist.is_public or 
                playlist.owner == request.user or 
                request.user in playlist.collaborators.all() or
                request.user in playlist.followers.all())
    
    elif action == 'edit':
        return (playlist.owner == request.user or 
                request.user in playlist.collaborators.all() or
                str(request.user.id) in playlist.could_edit)
    
    elif action == 'delete':
        return playlist.owner == request.user
    
    return False

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get public playlists")
def playlist_list(request):
    """Get list of public playlists"""
    try:
        playlists = Playlist.objects.filter(is_public=True)[:20]
        playlists_data = []
        
        for playlist in playlists:
            playlists_data.append({
                'id': playlist.id,
                'name': playlist.name,
                'owner': playlist.owner.name,
                'track_count': len(playlist.tracks),
                'followers_count': playlist.followers.count(),
                'created_at': playlist.date.isoformat(),
            })
        
        return Response(playlists_data)
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get user's playlists")
def user_playlists(request):
    """Get user's own playlists"""
    try:
        playlists = Playlist.objects.filter(owner=request.user)
        playlists_data = []
        
        for playlist in playlists:
            playlists_data.append({
                'id': playlist.id,
                'name': playlist.name,
                'track_count': len(playlist.tracks),
                'followers_count': playlist.followers.count(),
                'is_public': playlist.is_public,
                'created_at': playlist.date.isoformat(),
            })
        
        return Response(playlists_data)
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get playlist details")
def playlist_detail(request, playlist_id):
    """Get specific playlist details"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if not check_playlist_permission(request, playlist, 'view'):
            print(1)
            #return Response({'error': 'Access denied'}, status=status.HTTP_403_FORBIDDEN)
        
        # Get collaborators info
        collaborators = []
        for collaborator in playlist.collaborators.all():
            collaborators.append({
                'id': collaborator.id,
                'name': collaborator.name,
                'avatar': collaborator.avatar
            })
        
        playlist_data = {
            'id': playlist.id,
            'name': playlist.name,
            'owner': {
                'id': playlist.owner.id,
                'name': playlist.owner.name,
                'avatar': playlist.owner.avatar
            },
            'tracks': playlist.tracks,
            'track_count': len(playlist.tracks),
            'is_public': playlist.is_public,
            'followers_count': playlist.followers.count(),
            'collaborators': collaborators,
            'could_edit': playlist.could_edit,
            'created_at': playlist.date.isoformat(),
        }
        
        return Response(playlist_data)
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@swagger_auto_schema(
    method='post',
    operation_summary="Create new playlist",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'name': openapi.Schema(type=openapi.TYPE_STRING),
            'is_public': openapi.Schema(type=openapi.TYPE_BOOLEAN),
        }
    )
)
@api_view(['POST'])
def create_playlist(request):
    """Create a new playlist"""
    try:
        name = request.data.get('name')
        is_public = request.data.get('is_public', True)
        
        if not name:
            return Response({'error': 'Playlist name is required'}, status=status.HTTP_400_BAD_REQUEST)
        playlist = Playlist.objects.create(
            name=name,
            owner=request.user,
            is_public=is_public
        )
        
        return Response({
            'id': playlist.id,
            'name': playlist.name,
            'message': 'Playlist created successfully'
        }, status=status.HTTP_201_CREATED)
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@swagger_auto_schema(
    method='put',
    operation_summary="Update playlist",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'name': openapi.Schema(type=openapi.TYPE_STRING),
            'is_public': openapi.Schema(type=openapi.TYPE_BOOLEAN),
        }
    )
)
@api_view(['PUT'])
def update_playlist(request, playlist_id):
    """Update playlist details"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if not check_playlist_permission(request, playlist, 'edit'):
            return Response({'error': 'Permission denied'}, status=status.HTTP_403_FORBIDDEN)
        
        name = request.data.get('name')
        is_public = request.data.get('is_public')
        
        if name:
            playlist.name = name
        if is_public is not None:
            playlist.is_public = is_public
        
        playlist.save()
        
        return Response({'message': 'Playlist updated successfully'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['DELETE'])
@swagger_auto_schema(operation_summary="Delete playlist")
def delete_playlist(request, playlist_id):
    """Delete a playlist"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if not check_playlist_permission(request, playlist, 'delete'):
            return Response({'error': 'Only playlist owner can delete'}, status=status.HTTP_403_FORBIDDEN)
        
        playlist.delete()
        return Response({'message': 'Playlist deleted successfully'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@swagger_auto_schema(operation_summary="Follow playlist")
def follow_playlist(request, playlist_id):
    """Follow a playlist"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if not playlist.is_public and playlist.owner != request.user:
            return Response({'error': 'Cannot follow private playlist'}, status=status.HTTP_403_FORBIDDEN)
        
        if playlist.owner == request.user:
            return Response({'error': 'Cannot follow your own playlist'}, status=status.HTTP_400_BAD_REQUEST)
        
        playlist.add_follower(request.user)
        return Response({'message': 'Playlist followed successfully'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['DELETE'])
@swagger_auto_schema(operation_summary="Unfollow playlist")
def unfollow_playlist(request, playlist_id):
    """Unfollow a playlist"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        playlist.remove_follower(request.user)
        return Response({'message': 'Playlist unfollowed successfully'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Add collaborator to playlist")
def add_collaborator(request, playlist_id, user_id):
    """Add a collaborator to playlist"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        user_to_add = get_object_or_404(User, id=user_id)
        
        if playlist.owner != request.user:
            return Response({'error': 'Only playlist owner can add collaborators'}, status=status.HTTP_403_FORBIDDEN)
        
        if user_to_add == playlist.owner:
            return Response({'error': 'Owner is already a collaborator'}, status=status.HTTP_400_BAD_REQUEST)
        
        # Check if user is already a collaborator
        if user_to_add in playlist.collaborators.all():
            return Response({'error': f'User {user_to_add.name} is already a collaborator'}, status=status.HTTP_400_BAD_REQUEST)
        
        playlist.add_collaborator(user_to_add)
        return Response({'message': f'User {user_to_add.name} added as collaborator'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['DELETE'])
@swagger_auto_schema(operation_summary="Remove collaborator from playlist")
def remove_collaborator(request, playlist_id, user_id):
    """Remove a collaborator from playlist"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        user_to_remove = get_object_or_404(User, id=user_id)
        
        if playlist.owner != request.user:
            return Response({'error': 'Only playlist owner can remove collaborators'}, status=status.HTTP_403_FORBIDDEN)
        
        if user_to_remove in playlist.collaborators.all():
            playlist.collaborators.remove(user_to_remove)
            return Response({'message': f'User {user_to_remove.name} removed as collaborator'})
        else:
            return Response({'message': 'User is not a collaborator'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@swagger_auto_schema(operation_summary="Add track to playlist")
def add_track_to_playlist(request, playlist_id, track_id):
    """Add a track to playlist"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if not check_playlist_permission(request, playlist, 'edit'):
            return Response({'error': 'Permission denied'}, status=status.HTTP_403_FORBIDDEN)
        
        if str(track_id) not in playlist.tracks:
            playlist.add_track(str(track_id))
            return Response({'message': 'Track added successfully'})
        else:
            return Response({'message': 'Track already in playlist'})
            
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['DELETE'])
@swagger_auto_schema(operation_summary="Remove track from playlist")
def remove_track_from_playlist(request, playlist_id, track_id):
    """Remove a track from playlist"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if not check_playlist_permission(request, playlist, 'edit'):
            return Response({'error': 'Permission denied'}, status=status.HTTP_403_FORBIDDEN)
        
        if str(track_id) in playlist.tracks:
            playlist.remove_track(str(track_id))
            return Response({'message': 'Track removed successfully'})
        else:
            return Response({'message': 'Track not in playlist'})
            
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get playlist tracks with details")
def get_playlist_tracks(request, playlist_id):
    """Get tracks in a playlist with full details from Jamendo"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if not check_playlist_permission(request, playlist, 'view'):
            return Response({'error': 'Access denied'}, status=status.HTTP_403_FORBIDDEN)
        
        # Get track details from Jamendo API
        tracks_data = get_playlist_songs(playlist_id)
        
        # Return playlist info along with track details
        response_data = {
            'playlist_info': {
                'id': playlist.id,
                'name': playlist.name,
                'owner': playlist.owner.name,
                'track_count': len(playlist.tracks),
                'is_public': playlist.is_public,
                'followers_count': playlist.followers.count(),
            },
            'tracks': tracks_data.get('results', []) if tracks_data else []
        }
        return Response(response_data)
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['PUT'])
@swagger_auto_schema(
    operation_summary="Change playlist visibility",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'is_public': openapi.Schema(type=openapi.TYPE_BOOLEAN),
        }
    )
)
def change_playlist_visibility(request, playlist_id):
    """Change playlist visibility (public/private)"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if playlist.owner != request.user:
            return Response({'error': 'Only playlist owner can change visibility'}, status=status.HTTP_403_FORBIDDEN)
        
        is_public = request.data.get('is_public')
        if is_public is not None:
            playlist.set_status(is_public)
            visibility = "public" if is_public else "private"
            return Response({'message': f'Playlist visibility changed to {visibility}'})
        else:
            return Response({'error': 'is_public field is required'}, status=status.HTTP_400_BAD_REQUEST)
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get playlist followers")
def playlist_followers(request, playlist_id):
    """Get list of playlist followers"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if not check_playlist_permission(request, playlist, 'view'):
            return Response({'error': 'Access denied'}, status=status.HTTP_403_FORBIDDEN)
        
        followers = []
        for follower in playlist.followers.all():
            followers.append({
                'id': follower.id,
                'name': follower.name,
                'avatar': follower.avatar,
            })
        
        return Response(followers)
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get followed playlists")
def followed_playlists(request):
    """Get playlists that user follows"""
    try:
        followed = Playlist.objects.filter(followers=request.user)
        playlists_data = []
        
        for playlist in followed:
            playlists_data.append({
                'id': playlist.id,
                'name': playlist.name,
                'owner': playlist.owner.name,
                'track_count': len(playlist.tracks),
                'followers_count': playlist.followers.count(),
                'created_at': playlist.date.isoformat(),
            })
        
        return Response(playlists_data)
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['PUT'])
@swagger_auto_schema(
    operation_summary="Reorder playlist tracks",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'track_order': openapi.Schema(type=openapi.TYPE_ARRAY, items=openapi.Schema(type=openapi.TYPE_STRING)),
        }
    )
)
def reorder_playlist_tracks(request, playlist_id):
    """Reorder tracks in playlist"""
    try:
        playlist = get_object_or_404(Playlist, id=playlist_id)
        
        if not check_playlist_permission(request, playlist, 'edit'):
            return Response({'error': 'Permission denied'}, status=status.HTTP_403_FORBIDDEN)
        
        new_order = request.data.get('track_order', [])
        
        # Validate that all tracks in new order exist in playlist
        if set(new_order) == set(playlist.tracks):
            playlist.tracks = new_order
            playlist.save()
            return Response({'message': 'Playlist tracks reordered successfully'})
        else:
            return Response({'error': 'Invalid track order'}, status=status.HTTP_400_BAD_REQUEST)
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)