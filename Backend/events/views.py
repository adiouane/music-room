from django.shortcuts import render, get_object_or_404
from django.http import JsonResponse
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi

from .models import Events
from users.models import User

def check_event_permission(request, events, action='view'):
    """Check event permissions"""
    if not request.user.is_authenticated:
        return False
    
    if action == 'view':
        return (events.is_public or
                events.organizer == request.user or
                request.user in events.attendees.all())

    elif action == 'edit':
        return (events.organizer == request.user or
                str(request.user.id) in events.managers)

    elif action == 'delete':
        return events.organizer == request.user

    return False

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get public events")
def event_list(request):
    """Get list of public events"""
    try:
        events = Events.objects.filter(is_public=True)[:20]
        events_data = []
        
        for event in events:
            events_data.append({
                'id': event.id,
                'title': event.title,
                'organizer': event.organizer.name,
                'location': event.location,
                'attendee_count': event.attendee_count,
                'event_start_time': event.event_start_time.isoformat(),
                'is_public': event.is_public,
            })
        
        return Response(events_data)
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
#   @permission_classes([IsAuthenticated])
@swagger_auto_schema(
    operation_summary="Create new event",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'title': openapi.Schema(type=openapi.TYPE_STRING),
            'description': openapi.Schema(type=openapi.TYPE_STRING),
            'location': openapi.Schema(type=openapi.TYPE_STRING),
            'event_start_time': openapi.Schema(type=openapi.TYPE_STRING, format='datetime'),
            'event_end_time': openapi.Schema(type=openapi.TYPE_STRING, format='datetime'),
            'is_public': openapi.Schema(type=openapi.TYPE_BOOLEAN),
        }
    )
)
def create_event(request):
    """Create a new event"""
    try:
        title = request.data.get('title')
        description = request.data.get('description', '')
        location = request.data.get('location', '')
        event_start_time = request.data.get('event_start_time')
        event_end_time = request.data.get('event_end_time')
        is_public = request.data.get('is_public', True)
        
        if not title or not event_start_time:
            return Response({'error': 'Title and start time are required'}, status=status.HTTP_400_BAD_REQUEST)
        ss = User.objects.get(id=1)
        event = Events.objects.create(
            title=title,
            description=description,
            location=location,
            event_start_time=event_start_time,
            event_end_time=event_end_time,
            organizer=ss,
            is_public=is_public
        )
        
        return Response({
            'id': event.id,
            'title': event.title,
            'message': 'Event created successfully'
        }, status=status.HTTP_201_CREATED)
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get event details")
def event_detail(request, event_id):
    """Get specific event details"""
    try:
        event = get_object_or_404(Events, id=event_id)

        if not check_event_permission(request, event, 'view'):
            return Response({'error': 'Access denied'}, status=status.HTTP_403_FORBIDDEN)
        
        event_data = {
            'id': event.id,
            'title': event.title,
            'description': event.description,
            'location': event.location,
            'event_start_time': event.event_start_time.isoformat(),
            'event_end_time': event.event_end_time.isoformat() if event.event_end_time else None,
            'organizer': {
                'id': event.organizer.id,
                'name': event.organizer.name,
                'avatar': event.organizer.avatar
            },
            'attendee_count': event.attendee_count,
            'track_count': event.track_count,
            'is_public': event.is_public,
            'songs': event.songs,
        }
        
        return Response(event_data)
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Join event")
def join_event(request, event_id):
    """Join an event"""
    try:
        event = get_object_or_404(Events, id=event_id)

        if not event.is_public and event.organizer != request.user:
            return Response({'error': 'Cannot join private event'}, status=status.HTTP_403_FORBIDDEN)
        
        if event.add_attendee(request.user):
            return Response({'message': 'Successfully joined event'})
        else:
            return Response({'message': 'Already attending this event'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Leave event")
def leave_event(request, event_id):
    """Leave an event"""
    try:
        event = get_object_or_404(Events, id=event_id)

        if event.organizer == request.user:
            return Response({'error': 'Organizer cannot leave their own event'}, status=status.HTTP_400_BAD_REQUEST)
        
        if event.remove_attendee(request.user):
            return Response({'message': 'Successfully left event'})
        else:
            return Response({'message': 'Not attending this event'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Add user to event")
def add_user_to_event(request, event_id, user_id):
    """Add a user to an event (invite)"""
    try:
        event = get_object_or_404(Events, id=event_id)
        user_to_add = get_object_or_404(User, id=user_id)
        
        if not check_event_permission(request, event, 'edit'):
            return Response({'error': 'Only organizer or managers can add users'}, status=status.HTTP_403_FORBIDDEN)
        
        if event.add_attendee(user_to_add):
            return Response({'message': f'User {user_to_add.name} added successfully'})
        else:
            return Response({'message': 'User already attending this event'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Remove user from event")
def remove_user_from_event(request, event_id, user_id):
    """Remove a user from an event"""
    try:
        event = get_object_or_404(Events, id=event_id)
        user_to_remove = get_object_or_404(User, id=user_id)
        
        if not check_event_permission(request, event, 'edit'):
            return Response({'error': 'Only organizer or managers can remove users'}, status=status.HTTP_403_FORBIDDEN)
        
        if user_to_remove == event.organizer:
            return Response({'error': 'Cannot remove organizer from event'}, status=status.HTTP_400_BAD_REQUEST)
        
        if event.remove_attendee(user_to_remove):
            return Response({'message': f'User {user_to_remove.name} removed successfully'})
        else:
            return Response({'message': 'User not attending this event'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Add track to event")
def add_track_to_event(request, event_id, track_id):
    """Add a track to event"""
    try:
        event = get_object_or_404(Events, id=event_id)

        # Check if user is attendee or organizer
        if not (event.organizer == request.user or request.user in event.attendees.all()):
            return Response({'error': 'Only attendees can add tracks'}, status=status.HTTP_403_FORBIDDEN)
        
        if event.add_track(track_id):
            return Response({'message': 'Track added successfully'})
        else:
            return Response({'message': 'Track already in event'})
            
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Remove track from event")
def remove_track_from_event(request, event_id, track_id):
    """Remove a track from event"""
    try:
        event = get_object_or_404(Events, id=event_id)

        # Only organizer can remove tracks
        if not check_event_permission(request, event, 'edit'):
            return Response({'error': 'Only organizer or managers can remove tracks'}, status=status.HTTP_403_FORBIDDEN)
        
        if event.remove_track(track_id):
            return Response({'message': 'Track removed successfully'})
        else:
            return Response({'message': 'Track not in event'})
            
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get event tracks")
def get_event_tracks(request, event_id):
    """Get tracks in an event with vote counts"""
    try:
        event = get_object_or_404(Events, id=event_id)

        if not check_event_permission(request, event, 'view'):
            return Response({'error': 'Access denied'}, status=status.HTTP_403_FORBIDDEN)
        
        tracks_with_votes = []
        for track_id in event.songs:
            tracks_with_votes.append({
                'track_id': track_id,
                'votes': event.get_track_votes(track_id)
            })
        
        return Response({'tracks': tracks_with_votes})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['POST'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Vote for track in event")
def vote_track_in_event(request, event_id, track_id):
    """Vote for a track in event"""
    try:
        event = get_object_or_404(Events, id=event_id)

        # Only attendees can vote
        if not (event.organizer == request.user or request.user in event.attendees.all()):
            return Response({'error': 'Only attendees can vote'}, status=status.HTTP_403_FORBIDDEN)
        
        if event.vote_track(request.user.id, track_id):
            return Response({'message': 'Vote added successfully'})
        else:
            return Response({'message': 'Already voted for this track or track not in event'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['DELETE'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Remove vote for track in event")
def unvote_track_in_event(request, event_id, track_id):
    """Remove vote for a track in event"""
    try:
        event = get_object_or_404(Events, id=event_id)
        
        # Only attendees can unvote
        if not (event.organizer == request.user or request.user in event.attendees.all()):
            return Response({'error': 'Only attendees can unvote'}, status=status.HTTP_403_FORBIDDEN)
        
        if event.unvote_track(request.user.id, track_id):
            return Response({'message': 'Vote removed successfully'})
        else:
            return Response({'message': 'Vote not found'})
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['PUT'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Change event visibility")
def change_event_visibility(request, event_id):
    """Change event visibility (public/private)"""
    try:
        event = get_object_or_404(Events, id=event_id)
        
        if not check_event_permission(request, event, 'edit'):
            return Response({'error': 'Only organizer or managers can change visibility'}, status=status.HTTP_403_FORBIDDEN)
        
        is_public = request.data.get('is_public')
        if is_public is not None:
            event.is_public = is_public
            event.save()
            visibility = "public" if is_public else "private"
            return Response({'message': f'Event visibility changed to {visibility}'})
        else:
            return Response({'error': 'is_public field is required'}, status=status.HTTP_400_BAD_REQUEST)
        
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get event attendees")
def event_attendees(request, event_id):
    """Get list of event attendees"""
    try:
        event = get_object_or_404(Events, id=event_id)
        
        if not check_event_permission(request, event, 'view'):
            return Response({'error': 'Access denied'}, status=status.HTTP_403_FORBIDDEN)
        
        attendees = []
        for attendee in event.attendees.all():
            attendees.append({
                'id': attendee.id,
                'name': attendee.name,
                'avatar': attendee.avatar,
            })
        
        return Response(attendees)
    except Exception as e:
        return Response({'error': str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
