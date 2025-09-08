FROM alpine:3.18
RUN echo "https://dl-cdn.alpinelinux.org/alpine/edge/main/" > /etc/apk/repositories && \
    echo "https://dl-cdn.alpinelinux.org/alpine/edge/community/" >> /etc/apk/repositories
RUN apk update
RUN apk add --no-cache openjdk21-jdk apache-ant bash
CMD ["bash"]
