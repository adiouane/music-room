from django.urls import path
from . import views

urlpatterns = [
    # User CRUD operations
    path('users/', views.get_all_users_view, name='get_all_users'),
    path('create/', views.create_user_view, name='create_user'),
    path('<int:user_id>/', views.get_user_by_id_view, name='get_user_by_id'),
    path('<int:user_id>/update/', views.update_user_view, name='update_user'),
    path('<int:user_id>/delete/', views.delete_user_view, name='delete_user'),
    
    # User search
    path('search/', views.get_user_by_email_view, name='get_user_by_email'),
]