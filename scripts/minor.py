log = open('../app/build.gradle', "r").read()
versionCode = log.split("versionCode ")[1].split('\n')[0]
versionName = log.split("versionName ")[1].split('\n')[0]


def bumpVersionCode(version):
    return str(int(version) + 1)


def bumpVersionName(version):
    version = version.replace("\"", "")
    v = version.split(".")
    return '"' + v[0] + '.' + bumpVersionCode(v[1]) + '.0"'


log = log.replace("versionCode " + versionCode, "versionCode " + bumpVersionCode(versionCode))
log = log.replace("versionName " + versionName, "versionName " + bumpVersionName(versionName))

open('../app/build.gradle', "w").write(log)

import os

os.system('git add -A && git commit -m "v' + bumpVersionName(versionName) + '" && git tag v'
          + bumpVersionName(versionName) + ' && git push && git push --tags')