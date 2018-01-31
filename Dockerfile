
FROM java:8
EXPOSE 8080
VOLUME /tmp
ADD /target/TicketService.jar App.jar
RUN bash -c 'touch /App.jar'
ENTRYPOINT ["java","-jar","/App.jar"]
