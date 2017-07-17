FROM ubuntu

# Initial setup
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update

# Install utilities
RUN \ 
  apt-get install -y software-properties-common && \
  apt-get install -y wget

# Install Chrome
RUN \
  apt-get install libxss1 libappindicator1 libindicator7 -y && \
  wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb && \
  dpkg -i google-chrome*.deb || : && \
  apt-get install -f -y

# Install Java
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer
  
EXPOSE 9222