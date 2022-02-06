package org.vividus.jenkins;

def run(Map config) {
  
  // print config
  def safeConfig = defaultConfg() << config;

  println("safe " + safeConfig)

  def testsDir = safeConfig.testsDir ?: pwd();
  dir(testsDir) {

    checkoutScm(safeConfig.scm);

    def testProperties = safeConfig.testProperties.collect({"-Pvividus.${it}"}).join(' ');

    sh("./gradlew runS ${testProperties}");

    publishTestReport(safeConfig.reportName);
  }
}

private checkoutScm(Map scm) {
  println("scm" + scm)
  if (scm) {
    def git = scm.git;
    
    gitCheckout(git.url, git.branch);
  } else {
    // ?
  }
}

private gitCheckout(url, branch) {
  checkout(
    [
      $class: 'GitSCM',
      branches: [
        [
          name: branch
        ]
      ],
      extensions: [
        [
          $class: 'SubmoduleOption',
          disableSubmodules: false,
          parentCredentials: false,
          recursiveSubmodules: true,
          reference: '',
          trackingSubmodules: false
        ]
      ],
      userRemoteConfigs: [
        [
          url: url
        ]
      ]
    ]
  );
}

private publishTestReport(reportName) {
  archiveArtifacts(artifacts: 'output/reports/**', fingerprint: true, followSymlinks: false);
  publishHTML(
    [
      allowMissing: false,
      alwaysLinkToLastBuild: true,
      keepAll: true,
      reportDir: 'output/reports/allure',
      reportFiles: 'index.html',
      reportName: reportName,
      reportTitles: ''
    ]
  );
}

private defaultConfg() {
  return [
    reportName: 'Vividus Test Report',
    testProperties: []
  ]
}
