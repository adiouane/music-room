from django.urls import path
from . import views

urlpatterns = [
    # Basic user CRUD
    path('', views.get_all_users_view, name='get_all_users'),
    path('create/', views.create_user_view, name='create_user'),
    path('<uuid:user_id>/', views.get_user_by_id_view, name='get_user_by_id'),
    path('<uuid:user_id>/update/', views.update_user_view, name='update_user'),
    path('<uuid:user_id>/delete/', views.delete_user_view, name='delete_user'),
    path('email/', views.get_user_by_email_view, name='get_user_by_email'),
    
    # Authentication endpoints (merged from accounts)
    path('auth/register/', views.register_user_view, name='register_user'),
    path('auth/login/', views.login_jwt_view, name='login_jwt'),
    path('auth/logout/', views.logout_view, name='logout'),
    path('auth/verify-email/', views.verify_email_view, name='verify_email'),
    path('auth/password-reset/', views.password_reset_view, name='password_reset'),
    path('auth/password-reset-confirm/', views.password_reset_confirm_view, name='password_reset_confirm'),
    path('auth/social-link/', views.social_link_view, name='social_link'),
    
    # Profile endpoints
    path('profile/', views.profile_view, name='profile'),
    path('profile/update/', views.profile_update_view, name='profile_update'),
    
    # Legacy simple login (keep for backward compatibility)
    path('login/', views.login_user_view, name='simple_login'),
]