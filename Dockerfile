FROM quay.io/ukhomeofficedigital/openjdk8:v1.8.0.171

USER root

# Copy Test Repository into the Docker Image
COPY . /app

# Create gradle user
ENV USER user-gradle
ENV USER_ID 1001
ENV GROUP group-gradle

# Set gradle variables
ENV GRADLE_VERSION 4.6
ENV GRADLE_HOME /usr/local/gradle
ENV PATH ${PATH}:${GRADLE_HOME}/bin
ENV GRADLE_USER_HOME /home/${USER}

RUN yum install wget unzip -y -q

# Install gradle - we do this instead using the wrapper so that the running container doesn't have to perform the download of Gradle.
# This makes the container start faster and removes the dependency on the Gralde download being available.
WORKDIR /usr/local
RUN wget  https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip -q && \
    unzip -q gradle-$GRADLE_VERSION-bin.zip && \
    rm -f gradle-$GRADLE_VERSION-bin.zip && \
    ln -s gradle-$GRADLE_VERSION gradle
RUN groupadd ${GROUP} && \
    useradd ${USER} -g ${GROUP} -u ${USER_ID} && \
    chown -R ${USER}:${GROUP} /app ${GRADLE_HOME} ${GRADLE_USER_HOME}

WORKDIR /app

# Switch user to gradle user
USER $USER

ENTRYPOINT gradle build
