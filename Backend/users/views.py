from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.decorators import api_view
from rest_framework.permissions import IsAuthenticated
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi
from .services import (
    get_all_users, 
    get_user_by_id, 
    get_user_by_email, 
    create_user, 
    update_user, 
    delete_user,
    login_user,
    # New authentication functions
    register_user,
    login_user_jwt,
    logout_user,
    verify_email,
    reset_password_request,
    reset_password_confirm,
    link_social_account,
    get_user_profile,
    update_user_profile
)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get all users")
def get_all_users_view(request):
    """Get list of all users"""
    users = get_all_users()
    if 'error' in users:
        return Response(users, status=status.HTTP_400_BAD_REQUEST)
    return Response(users, status=status.HTTP_200_OK)

@swagger_auto_schema(
    method='post',
    operation_summary="Create a new user",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'name': openapi.Schema(type=openapi.TYPE_STRING, description="User's full name"),
            'email': openapi.Schema(type=openapi.TYPE_STRING, description="User's email address"),
            'avatar': openapi.Schema(type=openapi.TYPE_STRING, description="URL to user's avatar"),
            'password': openapi.Schema(type=openapi.TYPE_STRING, description="User's password"),
        },
        required=['name', 'email', 'password']
    ),
    responses={
        201: openapi.Response(description="User created successfully"),
        400: openapi.Response(description="Invalid input"),
    }
)
@api_view(['POST'])
def create_user_view(request):
    """Create a new user"""
    data = request.data
    name = data.get('name')
    email = data.get('email')
    avatar = data.get('avatar', 'default_avatar.png')
    password = data.get('password')

    if not all([name, email, password]):
        return Response({'error': 'Missing required fields'}, status=status.HTTP_400_BAD_REQUEST)

    user = create_user(name, email, avatar, password)

    if 'error' in user:
        return Response(user, status=status.HTTP_400_BAD_REQUEST)

    return Response(user, status=status.HTTP_201_CREATED)

# NEW AUTHENTICATION VIEWS FROM ACCOUNTS APP

@swagger_auto_schema(
    method='post',
    operation_summary="Register a new user",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'name': openapi.Schema(type=openapi.TYPE_STRING, description="User's full name"),
            'email': openapi.Schema(type=openapi.TYPE_STRING, description="User's email address"),
            'password': openapi.Schema(type=openapi.TYPE_STRING, description="User's password"),
            'password_confirm': openapi.Schema(type=openapi.TYPE_STRING, description="Password confirmation"),
            'avatar': openapi.Schema(type=openapi.TYPE_STRING, description="URL to user's avatar"),
        },
        required=['name', 'email', 'password', 'password_confirm']
    ),
    responses={
        201: openapi.Response(description="User registered successfully"),
        400: openapi.Response(description="Invalid input"),
    }
)
@api_view(['POST'])
def register_user_view(request):
    """Register a new user"""
    data = request.data
    name = data.get('name')
    email = data.get('email')
    password = data.get('password')
    password_confirm = data.get('password_confirm')
    avatar = data.get('avatar', 'default_avatar.png')

    if not all([name, email, password, password_confirm]):
        return Response({'error': 'Missing required fields'}, status=status.HTTP_400_BAD_REQUEST)
    
    if password != password_confirm:
        return Response({'error': 'Passwords do not match'}, status=status.HTTP_400_BAD_REQUEST)

    result = register_user(name, email, password, avatar=avatar)

    if 'error' in result:
        return Response(result, status=status.HTTP_400_BAD_REQUEST)

    return Response(result, status=status.HTTP_201_CREATED)

@swagger_auto_schema(
    method='post',
    operation_summary="Login with JWT tokens",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'email': openapi.Schema(type=openapi.TYPE_STRING, description="User's email"),
            'password': openapi.Schema(type=openapi.TYPE_STRING, description="User's password"),
        },
        required=['email', 'password']
    ),
    responses={
        200: openapi.Response(description="Login successful"),
        400: openapi.Response(description="Invalid credentials"),
    }
)
@api_view(['POST'])
def login_jwt_view(request):
    """Login user with JWT tokens"""
    data = request.data
    email = data.get('email')
    password = data.get('password')

    if not email or not password:
        return Response({'error': 'Email and password are required'}, status=status.HTTP_400_BAD_REQUEST)

    result = login_user_jwt(email, password)

    if 'error' in result:
        return Response(result, status=status.HTTP_401_UNAUTHORIZED)

    return Response(result, status=status.HTTP_200_OK)

@swagger_auto_schema(
    method='post',
    operation_summary="Logout user",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'refresh_token': openapi.Schema(type=openapi.TYPE_STRING, description="Refresh token"),
        },
        required=['refresh_token']
    )
)
@api_view(['POST'])
def logout_view(request):
    """Logout user by blacklisting refresh token"""
    refresh_token = request.data.get('refresh_token')
    
    if not refresh_token:
        return Response({'error': 'Refresh token is required'}, status=status.HTTP_400_BAD_REQUEST)
    
    result = logout_user(refresh_token)
    
    if 'error' in result:
        return Response(result, status=status.HTTP_400_BAD_REQUEST)
    
    return Response(result, status=status.HTTP_200_OK)

@swagger_auto_schema(
    method='post',
    operation_summary="Verify email",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'token': openapi.Schema(type=openapi.TYPE_STRING, description="Email verification token"),
        },
        required=['token']
    )
)
@api_view(['POST'])
def verify_email_view(request):
    """Verify user email with token"""
    token = request.data.get('token')
    
    if not token:
        return Response({'error': 'Token is required'}, status=status.HTTP_400_BAD_REQUEST)
    
    result = verify_email(token)
    
    if 'error' in result:
        return Response(result, status=status.HTTP_400_BAD_REQUEST)
    
    return Response(result, status=status.HTTP_200_OK)

@swagger_auto_schema(
    method='post',
    operation_summary="Request password reset",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'email': openapi.Schema(type=openapi.TYPE_STRING, description="User's email"),
        },
        required=['email']
    )
)
@api_view(['POST'])
def password_reset_view(request):
    """Request password reset"""
    email = request.data.get('email')
    
    if not email:
        return Response({'error': 'Email is required'}, status=status.HTTP_400_BAD_REQUEST)
    
    result = reset_password_request(email)
    
    if 'error' in result:
        return Response(result, status=status.HTTP_400_BAD_REQUEST)
    
    return Response(result, status=status.HTTP_200_OK)

@swagger_auto_schema(
    method='post',
    operation_summary="Confirm password reset",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'token': openapi.Schema(type=openapi.TYPE_STRING, description="Reset token"),
            'password': openapi.Schema(type=openapi.TYPE_STRING, description="New password"),
            'password_confirm': openapi.Schema(type=openapi.TYPE_STRING, description="Password confirmation"),
        },
        required=['token', 'password', 'password_confirm']
    )
)
@api_view(['POST'])
def password_reset_confirm_view(request):
    """Confirm password reset"""
    data = request.data
    token = data.get('token')
    password = data.get('password')
    password_confirm = data.get('password_confirm')
    
    if not all([token, password, password_confirm]):
        return Response({'error': 'Missing required fields'}, status=status.HTTP_400_BAD_REQUEST)
    
    if password != password_confirm:
        return Response({'error': 'Passwords do not match'}, status=status.HTTP_400_BAD_REQUEST)
    
    result = reset_password_confirm(token, password)
    
    if 'error' in result:
        return Response(result, status=status.HTTP_400_BAD_REQUEST)
    
    return Response(result, status=status.HTTP_200_OK)

@swagger_auto_schema(
    method='post',
    operation_summary="Link social account",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'provider': openapi.Schema(type=openapi.TYPE_STRING, enum=['facebook', 'google']),
            'access_token': openapi.Schema(type=openapi.TYPE_STRING, description="Social media access token"),
        },
        required=['provider', 'access_token']
    )
)
@api_view(['POST'])
def social_link_view(request):
    """Link social media account"""
    if not request.user.is_authenticated:
        return Response({'error': 'Authentication required'}, status=status.HTTP_401_UNAUTHORIZED)
    
    data = request.data
    provider = data.get('provider')
    access_token = data.get('access_token')
    
    if not all([provider, access_token]):
        return Response({'error': 'Provider and access_token are required'}, status=status.HTTP_400_BAD_REQUEST)
    
    if provider not in ['facebook', 'google']:
        return Response({'error': 'Invalid provider'}, status=status.HTTP_400_BAD_REQUEST)
    
    result = link_social_account(request.user, provider, access_token)
    
    if 'error' in result:
        return Response(result, status=status.HTTP_400_BAD_REQUEST)
    
    return Response(result, status=status.HTTP_200_OK)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get user profile")
def profile_view(request):
    """Get current user profile"""
    if not request.user.is_authenticated:
        return Response({'error': 'Authentication required'}, status=status.HTTP_401_UNAUTHORIZED)
    
    profile = get_user_profile(request.user)
    return Response(profile, status=status.HTTP_200_OK)

@swagger_auto_schema(
    method='put',
    operation_summary="Update user profile",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'name': openapi.Schema(type=openapi.TYPE_STRING),
            'avatar': openapi.Schema(type=openapi.TYPE_STRING),
            'bio': openapi.Schema(type=openapi.TYPE_STRING),
            'date_of_birth': openapi.Schema(type=openapi.TYPE_STRING, format='date'),
            'phone_number': openapi.Schema(type=openapi.TYPE_STRING),
            'profile_privacy': openapi.Schema(type=openapi.TYPE_STRING),
            'email_privacy': openapi.Schema(type=openapi.TYPE_STRING),
            'phone_privacy': openapi.Schema(type=openapi.TYPE_STRING),
            'music_preferences': openapi.Schema(type=openapi.TYPE_OBJECT),
            'liked_artists': openapi.Schema(type=openapi.TYPE_ARRAY, items=openapi.Schema(type=openapi.TYPE_STRING)),
            'liked_albums': openapi.Schema(type=openapi.TYPE_ARRAY, items=openapi.Schema(type=openapi.TYPE_STRING)),
            'liked_songs': openapi.Schema(type=openapi.TYPE_ARRAY, items=openapi.Schema(type=openapi.TYPE_STRING)),
            'genres': openapi.Schema(type=openapi.TYPE_ARRAY, items=openapi.Schema(type=openapi.TYPE_STRING)),
        }
    )
)
@api_view(['PUT'])
def profile_update_view(request):
    """Update user profile"""
    if not request.user.is_authenticated:
        return Response({'error': 'Authentication required'}, status=status.HTTP_401_UNAUTHORIZED)
    
    result = update_user_profile(request.user, **request.data)
    
    if 'error' in result:
        return Response(result, status=status.HTTP_400_BAD_REQUEST)
    
    return Response({'message': 'Profile updated successfully', 'user': result}, status=status.HTTP_200_OK)

# Keep your existing views...
@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get user by ID")
def get_user_by_id_view(request, user_id):
    """Get user details by ID"""
    user = get_user_by_id(user_id)
    if 'error' in user:
        return Response(user, status=status.HTTP_404_NOT_FOUND)
    return Response(user, status=status.HTTP_200_OK)

@swagger_auto_schema(
    method='put',
    operation_summary="Update user",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        properties={
            'name': openapi.Schema(type=openapi.TYPE_STRING),
            'avatar': openapi.Schema(type=openapi.TYPE_STRING),
        }
    )
)
@api_view(['PUT'])
def update_user_view(request, user_id):
    """Update user information"""
    data = request.data
    user = update_user(user_id, **data)
    
    if 'error' in user:
        return Response(user, status=status.HTTP_400_BAD_REQUEST)
    
    return Response(user, status=status.HTTP_200_OK)

@api_view(['DELETE'])
@swagger_auto_schema(operation_summary="Delete user")
def delete_user_view(request, user_id):
    """Delete a user"""
    result = delete_user(user_id)
    if 'error' in result:
        return Response(result, status=status.HTTP_404_NOT_FOUND)
    
    return Response(result, status=status.HTTP_200_OK)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get user by email")
def get_user_by_email_view(request):
    """Get user by email"""
    email = request.GET.get('email')
    if not email:
        return Response({'error': 'Email parameter is required'}, status=status.HTTP_400_BAD_REQUEST)
    
    user = get_user_by_email(email)
    if 'error' in user:
        return Response(user, status=status.HTTP_404_NOT_FOUND)
    
    return Response(user, status=status.HTTP_200_OK)

@api_view(['POST'])
@swagger_auto_schema(operation_summary="Simple login (legacy)")
def login_user_view(request):
    """Simple login without JWT"""
    data = request.data
    email = data.get('email')
    password = data.get('password')

    if not email or not password:
        return Response({'error': 'Email and password are required'}, status=status.HTTP_400_BAD_REQUEST)

    result = login_user(email, password)

    if 'error' in result:
        return Response(result, status=status.HTTP_401_UNAUTHORIZED)

    return Response(result, status=status.HTTP_200_OK)
