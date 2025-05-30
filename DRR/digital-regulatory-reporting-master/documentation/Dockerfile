FROM regnosys-docker-registry.jfrog.io/regnosys/sphinx:latest
ARG LATEST_RELEASE_ID

RUN pip3 install sphinx-material
RUN pip3 install --upgrade myst-parser
RUN apt-get install jq -y

RUN mkdir /docs_source /docs_out /documentation
COPY . /documentation
WORKDIR /documentation

RUN echo "... LATEST_RELEASE_ID is ${LATEST_RELEASE_ID}"
RUN ./build.sh $LATEST_RELEASE_ID

## generate documentation
RUN /usr/bin/sphinx-build -n -b html build /docs_out

FROM nginx:1.21

## copy output to serve dir
COPY --from=0 /docs_out /usr/share/nginx/html
