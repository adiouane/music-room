from .models import User
from django.contrib.auth import authenticate
from rest_framework_simplejwt.tokens import RefreshToken
import jwt
from datetime import datetime, timedelta
from django.conf import settings
from django.core.mail import send_mail
import requests
from rest_framework.exceptions import ValidationError
from django.core.exceptions import ValidationError
from django.db import IntegrityError
from django.contrib.auth.hashers import make_password, check_password



def get_user_by_name(name):
    """Get a user by name"""
    try:
        user = User.objects.get(name=name, is_active=True)
        return {
            'id': user.id,
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
            'id': user.id,
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
            'id': user.id,
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
            'id': user.id,
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
            'id': user.id,
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
                    'id': user.id,
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

# NEW AUTHENTICATION FUNCTIONS FROM ACCOUNTS APP

def register_user(name, email, password, **extra_fields):
    """Register a new user with email verification"""
    try:
        if User.objects.filter(email=email).exists():
            return {'error': 'Email already exists'}
        
        user = User.objects.create_user(
            email=email,
            name=name,
            password=password,
            is_active=False,  # Require email verification
            is_verified=False,  # Set as unverified
            **extra_fields
        )
        
        # Generate email verification token
        token = jwt.encode({
            'user_id': user.id,
            'action': 'email_verification',
            'exp': datetime.utcnow() + timedelta(hours=24)
        }, settings.SECRET_KEY, algorithm='HS256')
        
        # Link points directly to backend verification endpoint
        verification_url = f"{settings.DEFAULT_API_URL}api/users/verify-email/?token={token}"
        send_mail(
            'Verify your MusicRoom account',
            f'Please click the link to verify your email: {verification_url}',
            settings.EMAIL_HOST_USER,
            [user.email],
            fail_silently=False,
        )
        
        return {
            'id': user.id,
            'name': user.name,
            'email': user.email,
            'avatar': user.avatar,
            'message': 'Registration successful. Please check your email to verify your account.'
        }
    except Exception as e:
        return {'error': str(e)}

def login_user_jwt(email, password):
    """Login user and return JWT tokens"""
    try:
        user = authenticate(email=email, password=password)
        if not user:
            return {'error': 'Invalid email or password'}
        
        if not user.is_active:
            return {'error': 'Account is not verified. Please check your email.'}
        
        refresh = RefreshToken.for_user(user)
        
        return {
            'user': {
                'id': user.id,
                'name': user.name,
                'email': user.email,
                'avatar': user.avatar
            },
            'tokens': {
                'refresh': str(refresh),
                'access': str(refresh.access_token),
            },
            'message': 'Login successful'
        }
    except Exception as e:
        return {'error': str(e)}

def verify_email(token):
    """Verify user email with token"""
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=["HS256"])
        user = User.objects.get(id=payload['user_id'])
        
        # Check if this is an email verification token
        if payload.get('action') != 'email_verification':
            return {'error': 'Invalid token type'}
        
        if not user.is_verified:
            user.is_active = True
            user.is_verified = True
            user.save()
            return {'message': 'Email verified successfully! You can now log in to your account.'}
        else:
            return {'message': 'Email already verified'}
            
    except jwt.ExpiredSignatureError:
        return {'error': 'Verification link has expired. Please request a new verification email.'}
    except (jwt.DecodeError, User.DoesNotExist):
        return {'error': 'Invalid verification link'}
    except Exception as e:
        return {'error': str(e)}

def logout_user(refresh_token):
    """Logout user by blacklisting refresh token"""
    try:
        token = RefreshToken(refresh_token)
        token.blacklist()
        return {'message': 'Successfully logged out'}
    except Exception:
        return {'error': 'Invalid token'}

def reset_password_request(email):
    """Send password reset email"""
    try:
        if not User.objects.filter(email=email).exists():
            return {'error': 'User with this email does not exist'}
        
        user = User.objects.get(email=email)
        
        # Generate password reset token
        token = jwt.encode({
            'user_id': user.id,
            'action': 'password_reset',
            'exp': datetime.utcnow() + timedelta(hours=1)
        }, settings.SECRET_KEY, algorithm='HS256')
        
        # Link points directly to backend password reset endpoint
        reset_url = f"{settings.DEFAULT_API_URL}api/users/password-reset-confirm/?token={token}"
        send_mail(
            'Reset your MusicRoom password',
            f'Please click the link to reset your password: {reset_url}',
            settings.EMAIL_HOST_USER,
            [user.email],
            fail_silently=False,
        )
        
        return {'message': 'Password reset email sent'}
    except Exception as e:
        return {'error': str(e)}

def reset_password_confirm(token, password):
    """Confirm password reset with token"""
    try:
        payload = jwt.decode(token, settings.SECRET_KEY, algorithms=["HS256"])
        user = User.objects.get(id=payload['user_id'])
        
        # Check if this is a password reset token
        if payload.get('action') != 'password_reset':
            return {'error': 'Invalid token type'}
        
        user.set_password(password)
        user.save()
        
        return {'message': 'Password reset successful! You can now log in with your new password.'}
    except jwt.ExpiredSignatureError:
        return {'error': 'Password reset link has expired. Please request a new password reset.'}
    except (jwt.DecodeError, User.DoesNotExist):
        return {'error': 'Invalid password reset link'}
    except Exception as e:
        return {'error': str(e)}

def link_social_account(user, provider, access_token):
    """Link social media account to user profile"""
    try:
        if provider == "facebook":
            social_id = verify_facebook_token(access_token)
            
            if User.objects.exclude(id=user.id).filter(facebook_id=social_id).exists():
                return {'error': 'This Facebook account is already linked to another user'}
            
            user.facebook_id = social_id
            user.save()
            
        elif provider == "google":
            social_id = verify_google_token(access_token)
            
            if User.objects.exclude(id=user.id).filter(google_id=social_id).exists():
                return {'error': 'This Google account is already linked to another user'}
            
            user.google_id = social_id
            user.save()
        
        return {'message': f'{provider.capitalize()} account linked successfully'}
    except Exception as e:
        return {'error': str(e)}

def verify_facebook_token(token):
    """Verify Facebook access token and return user ID"""
    url = f"https://graph.facebook.com/me?access_token={token}"
    response = requests.get(url)
    
    if response.status_code != 200:
        raise ValidationError("Invalid Facebook token")
    
    data = response.json()
    if 'id' not in data:
        raise ValidationError("Could not retrieve Facebook user ID")
    
    return data['id']

def verify_google_token(token):
    """Verify Google access token and return user ID"""
    url = f"https://oauth2.googleapis.com/tokeninfo?id_token={token}"
    response = requests.get(url)
    
    if response.status_code != 200:
        raise ValidationError("Invalid Google token")
    
    data = response.json()
    if 'sub' not in data:
        raise ValidationError("Could not retrieve Google user ID")
    
    return data['sub']

def get_user_profile(user):
    """Get user profile data"""
    return {
        'id': user.id,
        'email': user.email,
        'name': user.name,
        'avatar': user.avatar,
        'bio': user.bio,
        'date_of_birth': user.date_of_birth,
        'phone_number': user.phone_number,
        'profile_privacy': user.profile_privacy,
        'email_privacy': user.email_privacy,
        'phone_privacy': user.phone_privacy,
        'facebook_id': user.facebook_id,
        'google_id': user.google_id,
        'subscription_type': user.subscription_type,
        'is_premium': user.is_premium,
        'is_subscribed': user.is_subscribed,
        'music_preferences': user.music_preferences,
        'liked_artists': user.liked_artists,
        'liked_albums': user.liked_albums,
        'liked_songs': user.liked_songs,
        'genres': user.genres,
        'created_at': user.created_at
    }

def update_user_profile(user, **data):
    """Update user profile"""
    try:
        allowed_fields = [
            'name', 'avatar', 'bio', 'date_of_birth', 'phone_number',
            'profile_privacy', 'email_privacy', 'phone_privacy',
            'music_preferences', 'liked_artists', 'liked_albums', 
            'liked_songs', 'genres', 'last_song_played'
        ]
        
        for field, value in data.items():
            if field in allowed_fields and hasattr(user, field):
                setattr(user, field, value)
        
        user.save()
        return get_user_profile(user)
    except Exception as e:
        return {'error': str(e)}
    
def resend_verification_email(email):
    """Resend verification email to user"""
    try:
        user = User.objects.get(email=email)
        
        if user.is_verified:
            return {'error': 'Email is already verified'}
        
        # Generate new verification token
        token = jwt.encode({
            'user_id': user.id,
            'action': 'email_verification',
            'exp': datetime.utcnow() + timedelta(hours=24)
        }, settings.SECRET_KEY, algorithm='HS256')
        
        # Send verification email with backend link
        verification_url = f"{settings.DEFAULT_API_URL}api/users/verify-email/?token={token}"
        send_mail(
            'Verify your MusicRoom account',
            f'Please click the link to verify your email: {verification_url}',
            settings.EMAIL_HOST_USER,
            [user.email],
            fail_silently=False,
        )
        
        return {'message': 'Verification email sent successfully'}
    except User.DoesNotExist:
        return {'error': 'User with this email does not exist'}
    except Exception as e:
        return {'error': str(e)}