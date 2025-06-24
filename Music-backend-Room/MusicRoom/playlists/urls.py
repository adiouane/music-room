from django.urls import path
from . import views

urlpatterns = [
    # Playlist CRUD
    path('', views.playlist_list, name='playlist_list'),
    path('my/', views.user_playlists, name='user_playlists'),
    path('followed/', views.followed_playlists, name='followed_playlists'),
    path('create/', views.create_playlist, name='create_playlist'),
    path('<int:playlist_id>/', views.playlist_detail, name='playlist_detail'),
    path('<int:playlist_id>/update/', views.update_playlist, name='update_playlist'),
    path('<int:playlist_id>/delete/', views.delete_playlist, name='delete_playlist'),
    
    # Playlist following
    path('<int:playlist_id>/follow/', views.follow_playlist, name='follow_playlist'),
    path('<int:playlist_id>/unfollow/', views.unfollow_playlist, name='unfollow_playlist'),
    path('<int:playlist_id>/followers/', views.playlist_followers, name='playlist_followers'),
    
    # Playlist collaboration
    path('<int:playlist_id>/collaborators/<int:user_id>/add/', views.add_collaborator, name='add_collaborator'),
    path('<int:playlist_id>/collaborators/<int:user_id>/remove/', views.remove_collaborator, name='remove_collaborator'),
    
    # Playlist tracks
    path('<int:playlist_id>/tracks/', views.get_playlist_tracks, name='get_playlist_tracks'),
    path('<int:playlist_id>/tracks/<int:track_id>/add/', views.add_track_to_playlist, name='add_track_to_playlist'),
    path('<int:playlist_id>/tracks/<int:track_id>/remove/', views.remove_track_from_playlist, name='remove_track_from_playlist'),
    path('<int:playlist_id>/tracks/reorder/', views.reorder_playlist_tracks, name='reorder_playlist_tracks'),
    
    # Playlist settings
    path('<int:playlist_id>/visibility/', views.change_playlist_visibility, name='change_playlist_visibility'),
]