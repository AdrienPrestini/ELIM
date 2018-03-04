FROM node:8
WORKDIR /Users/Nicolas/Desktop/Elim/Projet_docker/docker
COPY package.json /Users/Nicolas/Desktop/Elim/Projet_docker/docker
RUN npm install
COPY . /Users/Nicolas/Desktop/Elim/Projet_docker/docker
CMD node index.js
EXPOSE 8080