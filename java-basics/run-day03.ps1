$ErrorActionPreference = 'Stop'
$srcDir = Join-Path $PSScriptRoot 'src'
Set-Location $srcDir
javac -encoding UTF-8 Day03ReviewScore.java
java Day03ReviewScore
