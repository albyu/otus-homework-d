FROM amazoncorretto:17
ARG APP_HOME=/opt/otus-homework
RUN mkdir $APP_HOME
WORKDIR $APP_HOME
COPY build/libs/otus-homework-d-notification.jar $APP_HOME/otus-homework-d-notification.jar
EXPOSE 8000
ENTRYPOINT ["java","-jar","otus-homework-d-notification.jar"]
