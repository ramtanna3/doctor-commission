#!/bin/bash

# Update system
sudo dnf update -y

# Install Java 21
sudo dnf install java-21-amazon-corretto java-21-amazon-corretto-devel maven mariadb105 git -y
# Install Node.js and npm
sudo dnf install nodejs npm -y

# Install additional tools
sudo dnf install git maven mysql -y

# Reload profile
source ~/.bashrc

# Verify installations
echo "=== Installation Verification ==="
java -version
node --version
npm --version
git --version
mvn --version
mysql --version
echo "JAVA_HOME: $JAVA_HOME"
