from .models import User
from django.core.exceptions import ValidationError
from django.db import IntegrityError
from django.contrib.auth.hashers import make_password, check_password

def get_all_users():
    """Get all active users"""
    try:
        users = User.objects.filter(is_active=True).values(
            'id', 'name', 'email', 'avatar', 'created_at', 'updated_at'
        )
        return list(users)
    except Exception as e:
        return {'error': str(e)}

def get_user_by_id(user_id):
    """Get a specific user by ID"""
    try:
        user = User.objects.get(id=user_id, is_active=True)
        return {
            'id': str(user.id),
            'name': user.name,
            'email': user.email,
            'avatar': user.avatar,
            'created_at': user.created_at,
            'updated_at': user.updated_at
        }
    except User.DoesNotExist:
        return {'error': 'User not found'}
    except Exception as e:
        return {'error': str(e)}

def get_user_by_email(email):
    """Get a user by email"""
    try:
        user = User.objects.get(email=email, is_active=True)
        return {
            'id': str(user.id),
            'name': user.name,
            'email': user.email,
            'avatar': user.avatar,
            'created_at': user.created_at,
            'updated_at': user.updated_at
        }
    except User.DoesNotExist:
        return {'error': 'User not found'}
    except Exception as e:
        return {'error': str(e)}

def create_user(name, email, avatar=None, password=None):
    """Create a new user"""
    try:
        # Check if user already exists
        if User.objects.filter(email=email).exists():
            return {'error': 'User with this email already exists'}
        
        user_data = {
            'email': email,
            'name': name,
            'avatar': avatar,
        }
        
        if password:
            user_data['password'] = make_password(password)
        
        user = User.objects.create(**user_data)
        
        return {
            'id': str(user.id),
            'name': user.name,
            'email': user.email,
            'avatar': user.avatar,
            'created_at': user.created_at,
            'message': 'User created successfully'
        }
    except IntegrityError:
        return {'error': 'User with this email already exists'}
    except Exception as e:
        return {'error': str(e)}

def update_user(user_id, **kwargs):
    """Update user information"""
    try:
        user = User.objects.get(id=user_id, is_active=True)
        
        for field, value in kwargs.items():
            if hasattr(user, field) and field not in ['id', 'created_at']:
                setattr(user, field, value)
        
        user.save()
        
        return {
            'id': str(user.id),
            'name': user.name,
            'email': user.email,
            'avatar': user.avatar,
            'updated_at': user.updated_at,
            'message': 'User updated successfully'
        }
    except User.DoesNotExist:
        return {'error': 'User not found'}
    except Exception as e:
        return {'error': str(e)}

def delete_user(user_id):
    """Soft delete a user"""
    try:
        user = User.objects.get(id=user_id)
        user.is_active = False
        user.save()
        return {'message': 'User deleted successfully'}
    except User.DoesNotExist:
        return {'error': 'User not found'}
    except Exception as e:
        return {'error': str(e)}

def login_user(email, password):
    """Authenticate user with email and password"""
    try:
        # Get user by email
        user = User.objects.get(email=email, is_active=True)
        print(f"User found: {user.name} with email {user.email}")
        if user.is_verified is False:
            return {'error': 'User is not verified'}
        # Check password
        if check_password(password, user.password):
            # Update last login
            from django.utils import timezone
            user.last_login = timezone.now()
            user.save()
            
            return {
                'success': True,
                'user': {
                    'id': str(user.id),
                    'name': user.name,
                    'email': user.email,
                    'avatar': user.avatar,
                    'created_at': user.created_at,
                    'updated_at': user.updated_at
                }
            }
        
        else:
            return {'error': 'Invalid password'}
            
    except User.DoesNotExist:
        return {'error': 'User not found'}
    except Exception as e:
        return {'error': str(e)}