# escape=`

# Use the latest Windows Server Core 2022 image.
FROM mcr.microsoft.com/windows/servercore:ltsc2022

# Restore the default Windows shell for correct batch processing.
SHELL ["cmd", "/S", "/C"]

COPY ./tools/ci/windows/.vsconfig C:\\.vsconfig

RUN `
    # Download the Build Tools bootstrapper.
    curl -SL --output vs_buildtools.exe https://aka.ms/vs/17/release/vs_buildtools.exe `
    `
    # Install Build Tools with the Microsoft.VisualStudio.Workload.AzureBuildTools workload, excluding workloads and components with known issues.
    && (start /w vs_buildtools.exe --quiet --wait --norestart --nocache `
        --installPath "%ProgramFiles(x86)%\Microsoft Visual Studio\2022\BuildTools" `
        --config C:\\.vsconfig`
        || IF "%ERRORLEVEL%"=="3010" EXIT 0) `
    `
    # Cleanup
    && del /q vs_buildtools.exe

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV PYTHON_VERSION=3.12.2
ENV PYTHON_INSTALL_PATH=C:\Python
ENV HOME=C:\Qaiha

ENV VENV_PATH=$HOME\qaiha-dev

ENV WORKSPACE=C:\Workspace
WORKDIR $WORKSPACE

ENV TOOLS_HOME=C:\Tools

RUN if not exist %TOOLS_HOME% mkdir %TOOLS_HOME%
RUN if not exist %HOME% mkdir %HOME%

# Install python
RUN `
    curl -L -o python-installer.exe https://www.python.org/ftp/python/%PYTHON_VERSION%/python-%PYTHON_VERSION%-amd64.exe `
    && python-installer.exe /quiet InstallAllUsers=1 TargetDir=%PYTHON_INSTALL_PATH% PrependPath=1 Include_pip=1 `
    && del /q python-installer.exe

# Install YQ (YAML Reader)
RUN `
    curl -L -o %TOOLS_HOME%\yq.exe https://github.com/mikefarah/yq/releases/download/v4.45.2/yq_windows_amd64.exe

# Install AWS
RUN `
    curl -L -o AWSCLIV2.msi https://awscli.amazonaws.com/AWSCLIV2.msi `
    && start /wait msiexec.exe /i AWSCLIV2.msi /qn /norestart `
    && del /q AWSCLIV2.msi

# Install Git
RUN `
    curl -L -o git-installer.exe https://github.com/git-for-windows/git/releases/download/v2.49.0.windows.1/Git-2.49.0-64-bit.exe `
    && start /wait git-installer.exe /VERYSILENT /NORESTART /NOCANCEL /SP- /CLOSEAPPLICATIONS /RESTARTAPPLICATIONS /COMPONENTS="icons,ext\reg\shellhere,assoc,assoc_sh" `
    && del /q git-installer.exe

RUN setx PATH "%TOOLS_HOME%;%PATH%"

COPY apps/versions.yaml $WORKSPACE/versions.yaml

RUN python -m pip install uv

SHELL ["powershell", "-Command", "$ErrorActionPreference='Stop';"]

RUN `
    $versionsFile = Join-Path $env:WORKSPACE 'versions.yaml'; `
    $QAIRT_SDK_LLM = (yq eval '.qairt_sdk_llm' $versionsFile).Trim(); `
    $QAIRT_SDK_URL = 'https://softwarecenter.qualcomm.com/api/download/software/sdks/Qualcomm_AI_Runtime_Community/All/' + $QAIRT_SDK_LLM + '/v' + $QAIRT_SDK_LLM + '.zip'; `
    Write-Host 'Downloading QAIRT SDK for LLMs (may take a few minutes)...'; `
    Write-Host '   SDK URL: ' + $QAIRT_SDK_URL; `
    `
    Set-Location $env:HOME; `
    curl.exe -L -o qairt.zip $QAIRT_SDK_URL; `
    Write-Host 'Finished!'; `
    tar -xf qairt.zip; `
    Remove-Item qairt.zip; `
    `
    $QAIRT_ROOT = $env:HOME + '\qairt\' + $QAIRT_SDK_LLM; `
    New-Item -ItemType Directory -Path ($env:HOME + '\qairt') -Force | Out-Null; `
    cmd /c mklink /D ($env:HOME + '\qairt\latest') "$QAIRT_ROOT"; `
    `
    Remove-Item -Recurse -Force ($QAIRT_ROOT + '\docs'); `
    `
    Get-ChildItem ($QAIRT_ROOT + '\lib') -Directory | Where-Object { `
        -not $_.Name.StartsWith('arm64x-windows') -and `
        -not $_.Name.StartsWith('aarch64-windows') -and `
        -not $_.Name.StartsWith('hexagon') `
    } | Remove-Item -Recurse -Force; `
    `
    Get-ChildItem ($QAIRT_ROOT + '\lib') -Filter '*-securepd*' -Recurse | Remove-Item -Force -Recurse; `
    `
    Get-ChildItem ($QAIRT_ROOT + '\bin') -Directory | Where-Object { `
        -not $_.Name.StartsWith('arm64x-windows') -and `
        -not $_.Name.StartsWith('aarch64-windows') `
    } | Remove-Item -Recurse -Force

ENV QAIRT_SDK_ROOT=$HOME\qairt\latest

# TODO: Standardize ENV var for QAIRT root for all apps
ENV QNN_SDK_ROOT=$QAIRT_SDK_ROOT
ENV VSDEV_CMD="C:\\Program Files (x86)\\Microsoft Visual Studio\\2022\\BuildTools\\Common7\\Tools\\VsDevCmd.bat"

RUN Remove-Item -Recurse -Force "$env:WORKSPACE\*"

# Define the entry point for the docker container.
# This entry point starts the developer command prompt and launches the PowerShell shell.
ENTRYPOINT ["powershell.exe", "-NoLogo", "-ExecutionPolicy", "Bypass"]
