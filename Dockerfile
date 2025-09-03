FROM alpine:3.14
RUN apk add --no-cache apache-ant bash
ENTRYPOINT ["bash"]
