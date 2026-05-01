$ErrorActionPreference = "Stop"

Write-Host "Creating Virtual Environment and Installing Deps"
. tools\setup_env.ps1 -Python C:\Python\python.exe -Venv $env:VENV_PATH

Write-Host "Activating Virtual Env ($env:VENV_PATH)"
. "$env:VENV_PATH`\Scripts\Activate.ps1"

Write-Host "Configuring Hub Credentials"
qai-hub configure --api_token "$env:HUB_USER_TOKEN_PROD" --no-verbose

Write-Host "Running Tests"
python -m pytest --pyargs qai_hub_apps_test.test.test_apps -s -vv -k windows

exit $LASTEXITCODE
