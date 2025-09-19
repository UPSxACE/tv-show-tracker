# Environment Variables
The following environment variables are accepted to set up the application:
```.env
DISCOVERY_ENABLED=true
DATABASE_NAME=tv_tracker_dev
DATABASE_USERNAME=postgres
DATABASE_PASSWORD=postgres
DATABASE_MAX_SIZE=20 #maximum size of the database in megabytes, before it stops fetching data from external api through discovery
TMDB_API_KEY={your_api_key}
```
