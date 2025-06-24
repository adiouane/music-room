from django.shortcuts import render, get_object_or_404
from django.http import JsonResponse
from rest_framework.decorators import api_view, permission_classes
from rest_framework.permissions import IsAuthenticated
from rest_framework.response import Response
from rest_framework import status
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi

from users.models import User
from music.services import get_jamendo_artists


@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get artist list")
def artist_list(request):
    """Get list of artists"""
    artists = get_jamendo_artists()
    return Response(artists)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get artist details")
def artist_detail(request, artist_id):
    """Get specific artist details"""
    # Implement artist detail logic
    return Response({"message": f"Artist {artist_id} details"})

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Search artists")
def artist_search(request, query):
    """Search for artists"""
    # Implement artist search logic
    return Response({"message": f"Search results for artists: {query}"})

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get popular artists")
def popular_artists(request):
    """Get popular artists"""
    limit = request.GET.get('limit', 10)
    # Implement popular artists logic
    return Response({"message": "Popular artists"})
