FROM python:3.12-slim-trixie

FROM ubuntu:24.04

SHELL ["/bin/bash", "-O", "extglob", "-c"]

ENV WORKSPACE=/workspace
WORKDIR $WORKSPACE

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV ANDROID_HOME=/android-sdk

###
# Root User
###
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
    git \
    git-lfs \
    ca-certificates \
    wget \
    curl \
    zip \
    unzip \
    python3 \
    python3.12-venv && \
    rm -rf /var/lib/apt/lists/*

# YQ (YAML Reader)
RUN wget https://github.com/mikefarah/yq/releases/latest/download/yq_linux_amd64 -O /usr/local/bin/yq && \
    chmod +x /usr/local/bin/yq

# AWS
RUN curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip" && \
    unzip -q awscliv2.zip && \
    ./aws/install && \
    rm -rf aws awscliv2.zip

# UV
COPY --from=ghcr.io/astral-sh/uv:latest /uv /uvx /bin/

# Copy Versions YAML
COPY apps/versions.yaml $WORKSPACE/versions.yaml

# Setup non-root user (use 1001 to play nicely with actions/checkout@v4)
RUN groupadd -g 1001 -r qaiha && useradd -r -m -u 1001 -g qaiha -s /bin/bash qaiha
RUN chown -R qaiha:qaiha $WORKSPACE
RUN mkdir $ANDROID_HOME && chown qaiha:qaiha $ANDROID_HOME
ENV HOME=/home/qaiha

###
# AI Hub Apps Test User
###

USER qaiha
ENV VENV_PATH=$HOME/qaiha-dev

# SDKs: Gradle, Java, Android, QAIRT
ARG SDKMAN_DIR="$HOME/.sdkman"
RUN curl "https://get.sdkman.io?ci=true" | /bin/bash

RUN JAVA_VERSION=$(yq eval '.java_sdk' $WORKSPACE/versions.yaml) && \
    source "$SDKMAN_DIR/bin/sdkman-init.sh" && \
    sdk install java $JAVA_VERSION
ENV PATH="$SDKMAN_DIR/candidates/java/current/bin:$PATH"
ENV JAVA_HOME="$SDKMAN_DIR/candidates/java/current"

RUN GRADLE_VERSION=$(yq eval '.gradle' $WORKSPACE/versions.yaml) && \
    source "$SDKMAN_DIR/bin/sdkman-init.sh" && \
    sdk install gradle $GRADLE_VERSION
ENV PATH="$SDKMAN_DIR/candidates/gradle/current/bin:$PATH"
ENV GRADLE_HOME="$SDKMAN_DIR/candidates/gradle/current"

RUN ANDROID_CMDLINE_TOOLS=$(yq eval '.android_cmdline_tools' $WORKSPACE/versions.yaml) && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_CMDLINE_TOOLS}.zip -O cmdline-tools.zip && \
    mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    unzip cmdline-tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm cmdline-tools.zip

RUN yes | ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager --licenses

RUN ANDROID_COMPILE_API=$(yq eval '.android_compile_api' $WORKSPACE/versions.yaml) && \
    ANDROID_TARGET_API=$(yq eval '.android_target_api' $WORKSPACE/versions.yaml) && \
    ANDROID_NDK=$(yq eval '.android_ndk' $WORKSPACE/versions.yaml) && \
    ${ANDROID_HOME}/cmdline-tools/latest/bin/sdkmanager "platform-tools" "build-tools;${ANDROID_COMPILE_API}.0.0" "platforms;android-${ANDROID_TARGET_API}" "ndk;${ANDROID_NDK}"

RUN QAIRT_SDK_LLM=$(yq eval '.qairt_sdk_llm' $WORKSPACE/versions.yaml) && \
    cd "$HOME" && \
    QAIRT_SDK_URL="https://softwarecenter.qualcomm.com/api/download/software/sdks/Qualcomm_AI_Runtime_Community/All/${QAIRT_SDK_LLM}/v${QAIRT_SDK_LLM}.zip" && \
    echo "Downloading QAIRT SDK for LLMs (may take a few minutes)..." && \
    echo "   SDK URL: $QAIRT_SDK_URL" && \
    wget -q -O qairt.zip "$QAIRT_SDK_URL" && \
    echo "Finished!" && \
    unzip -q qairt.zip && \
    rm qairt.zip && \
    QAIRT_ROOT="$HOME/qairt/$QAIRT_SDK_LLM" && \
    ln -s "$QAIRT_ROOT" "$HOME/qairt/latest" && \

    ###
    # The full QAIRT SDK is 12GB, which can cause out of disk space issues on standard GitHub runners.
    # We remove parts of the SDK that aren't used by the apps to save disk space.
    ###
    cd "$QAIRT_ROOT" && \
    rm -rf "docs" && \
    cd "$QAIRT_ROOT/lib" && \
    rm -rf !("aarch64-android"|"android"|hexagon-*) && \
    rm -rf *-securepd && \
    cd "$QAIRT_ROOT/bin" && \
    rm -rf !("aarch64-android")

ENV QAIRT_SDK_ROOT=$HOME/qairt/latest

###
# Cleanup
###

# We don't need to use SDKMan to install packages at runtime.
RUN rm -rf "$SDKMAN_DIR/bin"

# Delete the files we've copied; GitHub actions will clone the repo to a different location.
RUN rm -rf $WORKSPACE/*

WORKDIR $HOME
CMD ["bash"]
