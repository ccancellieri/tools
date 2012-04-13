#!/bin/sh

## clone

#Clone the “Spoon-Knife” project
# git clone git@github.com:username/Spoon-Knife.git

#Configure remotes
# When a repo is cloned, it has a default remote
# called origin that points to your fork on GitHub,
# not the original repo it was forked from. 
# To keep track of the original repo, you need to
# add another remote named upstream:
# cd Spoon-Knife
# git remote add upstream git://github.com/octocat/Spoon-Knife.git
# git fetch upstream

# update fork from main project
git push origin master
git fetch upstream
git merge upstream/master
