from django.shortcuts import render
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.decorators import api_view
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi
from .services import (
    get_all_users, 
    get_user_by_id, 
    get_user_by_email, 
    create_user, 
    update_user, 
    delete_user,
    login_user
)

@api_view(['GET'])
@swagger_auto_schema(operation_summary="Get all users")
def get_all_users_view(request):
    """Get list of all users"""
    users = get_all_users()
    if 'error' in users:
        return Response(users, status=status.HTTP_400_BAD_REQUEST)
    return Response(users, status=status.HTTP_200_OK)

from rest_framework.decorators import api_view
from rest_framework.response import Response
from rest_framework import status
from drf_yasg.utils import swagger_auto_schema
from drf_yasg import openapi
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
    avatar = 'data.get)'
    password = data.get('password')

    if not all([name, email, password]):
        return Response({'error': 'Missing required fields'}, status=status.HTTP_400_BAD_REQUEST)

    user = create_user(name, email, avatar, password)

    if 'error' in user:
        return Response(user, status=status.HTTP_400_BAD_REQUEST)

    return Response(user, status=status.HTTP_201_CREATED)

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
    """Get user by email address"""
    email = request.GET.get('email')
    if not email:
        return Response({'error': 'Email parameter is required'}, status=status.HTTP_400_BAD_REQUEST)
    
    user = get_user_by_email(email)
    if 'error' in user:
        return Response(user, status=status.HTTP_404_NOT_FOUND)
    
    return Response(user, status=status.HTTP_200_OK)


@swagger_auto_schema(
    method='post',
    operation_summary="User login",
    operation_description="Authenticate user with email and password",
    request_body=openapi.Schema(
        type=openapi.TYPE_OBJECT,
        required=['email', 'password'],
        properties={
            'email': openapi.Schema(
                type=openapi.TYPE_STRING,
                description="User's email address",
                example="john.doe@example.com"
            ),
            'password': openapi.Schema(
                type=openapi.TYPE_STRING,
                description="User's password",
                example="securePassword123"
            ),
        }
    ),
    responses={
        200: openapi.Response(
            description="Login successful",
            examples={
                'application/json': {
                    'success': True,
                    'message': 'Login successful',
                    'user': {
                        'id': 1,
                        'name': 'John Doe',
                        'email': 'john.doe@example.com',
                        'avatar': 'https://example.com/avatar.jpg'
                    }
                }
            }
        ),
        400: openapi.Response(
            description="Missing credentials",
            examples={
                'application/json': {
                    'success': False,
                    'error': 'Email and password are required'
                }
            }
        ),
        401: openapi.Response(
            description="Invalid credentials",
            examples={
                'application/json': {
                    'success': False,
                    'error': 'Invalid email or password'
                }
            }
        ),
        404: openapi.Response(
            description="User not found",
            examples={
                'application/json': {
                    'success': False,
                    'error': 'User not found'
                }
            }
        )
    }
)
@api_view(['POST'])
def login_user_view(request):
    """Login user with email and password"""
    try:
        data = request.data
        email = data.get('email')
        password = data.get('password')
        
        # Check if email and password are provided
        if not email or not password:
            return Response({
                'success': False,
                'error': 'Email and password are required'
            }, status=status.HTTP_400_BAD_REQUEST)
        
        # Authenticate user
        result = login_user(email, password)
        
        if 'error' in result:
            if result['error'] == 'User not found':
                return Response({
                    'success': False,
                    'error': 'User not found'
                }, status=status.HTTP_404_NOT_FOUND)
            else:
                return Response({
                    'success': False,
                    'error': 'Invalid email or password'
                }, status=status.HTTP_401_UNAUTHORIZED)
        
        # Login successful
        return Response({
            'success': True,
            'message': 'Login successful',
            'user': result['user']
        }, status=status.HTTP_200_OK)
        
    except Exception as e:
        return Response({
            'success': False,
            'error': str(e)
        }, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
