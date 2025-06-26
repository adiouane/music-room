from django.shortcuts import render
from django.http import JsonResponse
from music.services import get_user_playlists, get_playlists, get_jamendo_related, get_popular_artists, get_events
from music.service.tracks import get_random_songs, get_recent
from drf_yasg.utils import swagger_auto_schema
from rest_framework.decorators import api_view


@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get Home Page Data")
def home(request):
    user_playlists = get_user_playlists(2, 5) or {}
    playlists = get_playlists(5) or {}
    user_playlists = user_playlists | playlists
    
    recommended_songs = get_jamendo_related(2, 5) or []
    recently_listened = get_recent(5) or []
    popular_songs = get_random_songs(5) or []
    popular_artists = get_popular_artists(5) or []
    events = get_events(3) or []
    
    fullhome = {
        "user_playlists": user_playlists,
        "recommended_songs": recommended_songs,
        "popular_songs": popular_songs,
        "recently_listened": recently_listened,
        "popular_artists": popular_artists,
        "events": events
    }
    return JsonResponse(fullhome, safe=False)
