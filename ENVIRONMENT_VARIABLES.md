# Environment Variables
The following environment variables are accepted to set up the application:
```.env
FRONTEND_URL=http://localhost:3000
DISCOVERY_ENABLED=true
DATABASE_NAME=tv_tracker_dev
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
DATABASE_MAX_SIZE=9 #maximum size of the database in megabytes, before it stops fetching data from external api through discovery
TMDB_API_KEY={your_api_key}
JWT_SECRET=your_jwt_secret
JWT_ACCESS_TOKEN_DURATION=1 #time in minutes that the access token should last
JWT_REFRESH_TOKEN_DURATION=10080 #time in minutes that the refresh token should last
```
