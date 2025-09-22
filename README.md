# TV Show Tracker API

A Spring Boot application providing a GraphQL API for tracking TV shows, actors, genres, and user favorites.

The backend:
* GraphQL api
* Has a Data Collector module that imports data from external api to database on the background (up to configured limit of database size)
* Rate-limiting on external api calls
* Users can register, authenticate and save favorite tv shows
* Everyone can publicly query the movies and actors
* Filter movies by genre and sort by popularity, average votes, first air date, id or name
* Paginated responses
* Passwords encrypted with Argon2id
* Jwt-based authentication through cookies (access and refresh) inspired on Clerk architecture
    * also accepts access token on authentication header
    * refresh token is stored http-only cookie
    * access tokens expire in 60 seconds in production, but are long-living in development
* Most queries were optimized
* Most queries and mutations are n+1 safe
* Sends email through SMTP
* Automatic email recommendations each 2 days
* Endpoint to delete data to comply to RGPD
* Docker
* Unit tests with mocking for most relevant classes

The frontend:
* Fully responsive
* Server-side rendering and 24 hours cache
* Queries the backend with react-apollo, including server-side

Bonuses:
* GraphQL ✅
* Entity framework ✅ (JPA/hibernate is the java equivalent)
* Functional programming ✅
* Cryptography ✅
* Dependency Injection ✅

# Table of Contents

* [Requirements](#requirements)
* [Installation](#installation)
* [Running the API](#running-the-api)

    * [Development](#development)
    * [Production](#production)
* [GraphQL API](#graphql-api)
* [License](#license)

# Requirements

* Java 21
* Maven
* Docker & Docker Compose

# Installation

Clone the repository:

```bash
git clone https://github.com/your-username/tv-show-tracker.git
cd tv-show-tracker
```

Build the project with Maven:

```bash
mvn clean package
```

# Running the API

## Development Environment (With IntelliJ)

### Step 1: Open the Project in IntelliJ IDEA
1. Launch **IntelliJ IDEA**.
2. Select **File → Open…** and choose the root folder of your `tv-show-tracker` project.
3. Let IntelliJ import the project as a **Maven project**. Wait for dependencies to be downloaded.

### Step 2: Configure Spring Profiles

1. Go to **Run → Edit Configurations…**
2. Click **+ → Spring Boot** to add a new configuration.
3. Set **Name**: `TV Show Tracker Dev`
4. Set **Main class**: `com.upsxace.tv_show_tracker.TvShowTrackerApplication`

### Step 3: Set Environment Variables

1. In the same **Run/Debug Configuration**, scroll down to **Environment Variables**.
2. Click the **…** button and add the necessary environment variables.

I recommend setting at least these:
```env
DATABASE_MAX_SIZE=12
DATABASE_PASSWORD=postgres
DATABASE_USERNAME=postgres
DISCOVERY_ENABLED=true
JWT_SECRET=62f69b266d2bc3778e7a06c141ba0299 # example
SMTP_PASSWORD={smtpPassword}
TMDB_API_KEY={apiKey}
DATABASE_MAX_SIZE=10 # in megabytes
DISCOVERY_ENABLED=true # defaults to true
```
Check all the available options in [this document](/ENVIRONMENT_VARIABLES.md).

### Step 4: Run app
In the top right corner of your IDE you can run the application with the configured profile.

After the server is running:
* GraphiQL is enabled at: `http://localhost:8080/graphiql`
* Docker creates a postgres container with the database: `tv_tracker_dev`

## Production Environment

### 0. Compile the application
```cli
#linux
./mvnw clean package
# windows
./mvnw.cmd clean package
```
### 1. Set environment variables directly in your system
### 2. Start PostgreSQL using Docker
```bash
docker compose -f compose.production.yaml up -d
```
### 3. Run the application:
```bash
java -jar target/tv-show-tracker-{version}.jar --spring.profiles.active=prod
```

Please note:
* GraphiQL is disabled in production.
* Database: `tv_tracker` (configured in `.env`)

## GraphQL API

The API exposes the following operations:

### Queries

* `allTvShows(input: AllTvShowsInput): AllTvShowsPage!`
* `getTvShow(id: Int!): TvShow`
* `favoriteTvShows(input: FavoriteTvShowsInput!): FavoriteTvShowsPage!`
* `allActors(input: AllActorsInput): AllActorsPage!`
* `getActor(id: Int!): Actor`
* `getActorCredits(actorId: Int!): [ActorCredit!]!`
* `allGenres: [Genre!]!`

### Mutations

* `registerUser(input: RegisterUserInput!): Boolean!`
* `loginUser(input: LoginUserInput!): JwtResponse!`
* `refreshToken: JwtResponse`
* `logout: Boolean!`
* `saveFavoriteTvShow(tvShowId: Int!): Boolean!`
* `unfavoriteTvShow(tvShowId: Int!): Boolean!`
* `deleteAccount(input: ConfirmInput!): Boolean!`

> See the GraphQL schema for detailed input types, fields, and enums.

## License

This project is for demonstration purposes (Tekever technical challenge).

