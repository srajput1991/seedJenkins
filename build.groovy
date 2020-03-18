String PROJECT_NAMEPath = 'PROJECT_NAME'
String PROJECT_NAMEBuildPath = "$PROJECT_NAMEPath/Build"
String PROJECT_NAMEManualPath = "$PROJECT_NAMEPath/Manual"

String stashUrl = 'git_url'
String stashUser = 'USER'

def projects = [
    [project: 'PROJECT_NAME', url: stashUrl, user: stashUser, repo: 'peacebox-ova'],
    [project: 'PROJECT_NAME', url: stashUrl, user: stashUser, repo: 'dcos-cleanup']
]

folder(PROJECT_NAMEPath) {
    description 'PROJECT_NAME jobs.'
}

folder(PROJECT_NAMEBuildPath) {
    description 'PROJECT_NAME autobuild jobs.'
}

folder(PROJECT_NAMEManualPath) {
    description 'PROJECT_NAME manual jobs.'
}

listView('PROJECT_NAME develop') {
    description('All build jobs for PROJECT_NAME')
    recurse()
    jobs {
        regex(/$PROJECT_NAMEBuildPath\/.*\/develop/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

listView('PROJECT_NAME master') {
    description('All build jobs for PROJECT_NAME')
    recurse()
    jobs {
        regex(/$PROJECT_NAMEBuildPath\/.*\/master/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

listView('PROJECT_NAME all') {
    description('All build jobs for PROJECT_NAME')
    recurse()
    jobs {
        regex(/$PROJECT_NAMEBuildPath\/.*\/.*/)
    }
    columns {
        status()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
        buildButton()
    }
}

projects.each{ Map entry ->
    multibranchPipelineJob("$PROJECT_NAMEBuildPath/${entry.repo} autobuild") {
        branchSources {
            branchSource {
                source {
                    git {
                        browser {
                            stash {
                                repoUrl("${entry.url}")
                            }
                        }
                        remote("${entry.url}${entry.project}/${entry.repo}.git")
                        credentialsId(entry.user)
                        id("git")
                        traits {
                            authorInChangelogTrait()
                            wipeWorkspaceTrait()
                            pruneStaleBranchTrait()
                            localBranchTrait()
                            submoduleOptionTrait {
                                extension{
                                    disableSubmodules(false)
                                    recursiveSubmodules(true)
                                    trackingSubmodules(false)
                                    reference("")
                                    parentCredentials(true)
                                    timeout(60)
                                }
                            }


                            mercurialBrowserSCMSourceTrait {
                                browser {
                                    bitBucket {

                                        url("https://git.barco.com")
                                    }
                            }


                            cloneOptionTrait {
                                extension {
                                    shallow(false)
                                    noTags(false)
                                    reference("")
                                    timeout(60)
                                    honorRefspec(true)
                                }
                            }

                            refSpecsSCMSourceTrait {
                                templates {
                                    refSpecTemplate{
                                        value("+refs/heads/*:refs/remotes/origin/*")
                                    }
                                    refSpecTemplate{
                                        value("+refs/tags/*:refs/tags/*")
                                    }
                                }
                            }

                            remoteNameSCMSourceTrait{
                                remoteName("origin")
                            }

                            headWildcardFilter {
                                includes("*")
                                excludes("")
                            }

                        }
                    }


                }
            }
        }

        orphanedItemStrategy {
              discardOldItems {
                  daysToKeep(5)
                  numToKeep(5)
              }
        }

      }
      configure { node ->
          def data = node  / sources / data
          node  / sources / data / 'jenkins.branch.BranchSource' / source / traits / 'jenkins.plugins.git.traits.BranchDiscoveryTrait' {
                'switch'('on')
            }

      }

    }
    def job = jenkins.model.Jenkins.instance.getItemByFullName("$PROJECT_NAMEBuildPath/${entry.repo} autobuild")
    if(job){
      println ("start job : $PROJECT_NAMEBuildPath/${entry.repo} autobuild")
      job.scheduleBuild2(0)
    }
}
