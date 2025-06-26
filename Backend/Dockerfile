FROM python:3.12-slim

# Set environment variables
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Set work directory
WORKDIR /app

# Install system dependencies
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        postgresql-client \
        build-essential \
        libpq-dev \
        gcc \
        pkg-config \
    && rm -rf /var/lib/apt/lists/*

# Upgrade pip
RUN pip install --upgrade pip

# Copy requirements and install dependencies FIRST
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

# Verify Django installation
RUN python -c "import django; print(f'Django {django.get_version()} installed')"

# Copy project
COPY . /app/

# Create staticfiles and static directories
RUN mkdir -p /app/staticfiles
RUN mkdir -p /app/static

# Expose port
EXPOSE 8000

# Run the application
CMD ["python", "manage.py", "runserver", "0.0.0.0:8000"]