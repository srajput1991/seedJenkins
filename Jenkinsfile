node {
    checkout scm
    jobDsl targets: ['jobs/*.groovy'].join('\n'),
        removedJobAction: 'DELETE',
        removedViewAction: 'DELETE'
}
