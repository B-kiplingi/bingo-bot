# Bingo Bot for Discord

![Bingo Card Table](./card_example.png)

## Usage
`/bingo-start` Starts a new round of bingo. It loads all messages from a channel named pool and randomly picks 25 to be on the cards for the round.<br>
`/bingo-join` Registers you to participate in that round and generates your bingo card.<br>
`/bingo-card` Shows your bingo card if you are participating in a round.<br>
`/bingo-check A1` Marks A1 as completed on your bingo card.<br>

## Self Hosting with docker
### Prerequisites
- docker, docker compose
- some way to build a java app
### 1. Set up the bot on discord
set up the bot at https://discord.com/login?redirect_to=%2Fdevelopers%2Fapplications<br>
the bot requires `Server Members Intent` and `Message Content Intent`
Make sure to note the token, it'll be needed later.
### 1. Obtain jar
Clone the project
```
#bash
git clone https://github.com/B-kiplingi/bingo-bot.git
```
then build it in somehow and voilà, you've got bingo.jar
### 2. Docker setup
make a folder for all the files and navigate to it
```
#bash
mkdir bingo-bot
cd ./bingo-bot
```
the final structure should be like this
```
bingo-bot
├─bingo.jar
├─config
├─data
│ └─bingo-state.json
├─docker-compose.yml
├─Dockerfile
└─.env
```
Dockerfile
```
# Use a lightweight OpenJDK base image
FROM openjdk:25-jdk

# Set working directory
WORKDIR /app

# Copy your built JAR file into the container
COPY bingo.jar /app/bingo.jar

# Tell Docker what command to run when the container starts
CMD ["java", "-jar", "bingo.jar"]
```
docker-compose.yml
```
services:
  bingo-bot:
    build: .
    container_name: bingo-bot
    restart: unless-stopped
    env_file: .env
    volumes:
      # config mount
      - ./config:/app/config

      # persistent data mount
      - ./data:/app/data
```
.env
```
DISCORD_TOKEN=[your token here]
```
Now that everything's set up, you can build and run the container by running this command:<br>
`sudo docker compose up -d --build`<br>
The bot should now be running, and you can invite it to your server using the link on https://discord.com/login?redirect_to=%2Fdevelopers%2Fapplications under installation