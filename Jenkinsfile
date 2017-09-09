node {
	chmod +x gradlew
	checkout scm
	sh './gradlew setupCiWorkspace clean build'
	archive 'build/libs/*jar'
}
