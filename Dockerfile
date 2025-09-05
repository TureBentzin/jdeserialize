FROM alpine:3.18
RUN echo "https://dl-cdn.alpinelinux.org/alpine/v3.18/main/" > /etc/apk/repositories && \
    echo "https://dl-cdn.alpinelinux.org/alpine/v3.18/community/" >> /etc/apk/repositories
RUN apk update
RUN apk add --no-cache openjdk17-jdk apache-ant bash
CMD ["bash"]
