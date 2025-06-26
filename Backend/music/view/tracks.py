from django.shortcuts import render, get_object_or_404
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from drf_yasg.utils import swagger_auto_schema

from music.services import  get_jamendo_related
from music.service.tracks import get_jamendo_track,get_random_songs

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get song list")
def song_list(request):
    """Get list of songs from Jamendo"""
    songs = get_random_songs(limit=20)
    return Response(songs)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get song details")
def song_file(request, track_id):
    track_id = request.GET.get('track_id')
    song = get_jamendo_track(track_id)
    # Implement song detail logic
    return Response({"message": f"Song {song} details"})

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get related songs")
def related_list(request):
    """Get related songs"""
    user_id = request.GET.get('user_id', 1)
    limit = request.GET.get('limit', 10)
    related = get_jamendo_related(user_id, limit)
    return Response(related)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get random songs")
def random_songs(request):
    limit = request.GET.get('limit', 20)
    songs = get_random_songs(limit=limit)
    return Response(songs)

@api_view(['GET'])
@permission_classes([IsAuthenticated])
@swagger_auto_schema(operation_summary="Get user's recent tracks")
def recent_tracks(request, user_id):
    """Get user's recently played tracks"""
    # Implement recent tracks logic
    return Response({"message": f"Recent tracks for user {user_id}"})
