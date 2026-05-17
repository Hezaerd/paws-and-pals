$repo = "C:\Users\Admin\dev\minecraft\paws-and-pals"
$dest = "C:\Users\Admin\dev\minecraft\minecraft-src"

New-Item -ItemType Directory -Force -Path $dest | Out-Null

$srcJar = Get-ChildItem "$repo\.gradle" -Recurse -File |
  Where-Object { $_.Name -match 'minecraft.*26\.1\.1.*sources.*\.jar' } |
  Select-Object -First 1

if (-not $srcJar) {
  throw "No Minecraft sources jar found. Run .\gradlew genSources first, then search again."
}

Copy-Item $srcJar.FullName "$dest\minecraft-sources.zip" -Force
Expand-Archive "$dest\minecraft-sources.zip" -DestinationPath $dest -Force
Remove-Item "$dest\minecraft-sources.zip"