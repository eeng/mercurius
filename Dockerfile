FROM clojure:tools-deps

# Install npm
RUN apt-get update -yq \
  && apt-get install curl gnupg -yq \
  && curl -sL https://deb.nodesource.com/setup_12.x | bash \
  && apt-get install nodejs -yq

WORKDIR /app

# Install frontend dependencies
COPY package.json .
RUN npm i
COPY shadow-cljs.edn .
RUN npx shadow-cljs classpath > /dev/null

# Install backend deps
COPY deps.edn .
RUN clojure -Spath > /dev/null

# Build frontend
COPY src src
COPY resources resources
RUN npm run build

EXPOSE 5000/tcp

CMD ["/usr/local/bin/clojure", "-m", "mercurius.main"]
