# Twitch streamer popularity and stream characteristics
## Requirements
Java 8\
Intellij Community https://www.jetbrains.com/idea/download/#section=mac\
gCloud Command line tool https://cloud.google.com/sdk/gcloud\
Maven
## Get Started
Clone Repo\
Build with Intellij\
Setup startup to Main or to CloudFunction for testing Cloud Function entry point\
Run with Intellij
## Deployment
Deploy with gCloud or with Maven Cloud Functions library
## Repository Layout
TwitchClient - Logic for calling twitch and mapping from API data structure to java classes\
GcpClient - Consumes Google Cloud BigQuery and CloudStorage APIs using client library\
Streamer - Class representing the streamer data returned from Twitch API\
TwitchStreamSnapshot - A single instance of a stream's data return from Twitch API stream endpoint
