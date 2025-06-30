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

# Upgrade pip and install setuptools first
RUN pip install --upgrade pip setuptools>=65.0.0

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

# Create entrypoint script
RUN echo '#!/bin/bash\n\
echo "Waiting for database..."\n\
sleep 10\n\
echo "Running migrations..."\n\
python manage.py migrate --noinput\n\
echo "Collecting static files..."\n\
python manage.py collectstatic --noinput\n\
echo "Starting server..."\n\
python manage.py runserver 0.0.0.0:8000' > /app/entrypoint.sh

RUN chmod +x /app/entrypoint.sh

# Expose port
EXPOSE 8000

# Run the application
CMD ["/app/entrypoint.sh"]