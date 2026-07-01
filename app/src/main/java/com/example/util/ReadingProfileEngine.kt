package com.example.util

object ReadingProfileEngine {
    fun producePresentation(
        profile: ReadingProfile,
        state: DashboardState
    ): DashboardPresentation {
        val passText = when (state.currentPass) {
            MusicalPass.FIRST_PASS -> "1ª Passagem"
            MusicalPass.SECOND_PASS -> "2ª Passagem"
            MusicalPass.THIRD_PASS -> "3ª Passagem"
            MusicalPass.FINAL_PASS -> "Passagem Final"
        }

        val passShortText = when (state.currentPass) {
            MusicalPass.FIRST_PASS -> "PASSAGEM 1"
            MusicalPass.SECOND_PASS -> "PASSAGEM 2"
            MusicalPass.THIRD_PASS -> "PASSAGEM 3"
            MusicalPass.FINAL_PASS -> "FINAL"
        }

        val pagesUntil = state.pagesUntilInstruction?.let { "EM $it PÁGINAS" } ?: ""

        val emphasisPriority = if (state.pagesUntilInstruction != null && state.pagesUntilInstruction <= 1) {
            PresentationPriority.HIGH
        } else if (state.nextInstruction != null) {
            PresentationPriority.MEDIUM
        } else {
            PresentationPriority.LOW
        }

        return when (profile) {
            ReadingProfile.DEFAULT -> DashboardPresentation(
                title = state.currentSong.ifEmpty { "Música Avulsa" },
                subtitle = state.currentSection,
                badge = passText,
                cue = state.nextInstruction?.let { "$it $pagesUntil".trim() },
                emphasis = emphasisPriority
            )
            ReadingProfile.GUITAR -> DashboardPresentation(
                title = state.currentSection.uppercase(),
                subtitle = "Tom Original",
                badge = "Capo --",
                cue = state.nextInstruction?.let { "$it $pagesUntil".trim() },
                emphasis = emphasisPriority
            )
            ReadingProfile.PIANO -> DashboardPresentation(
                title = passText.uppercase(),
                subtitle = state.currentSection.uppercase(),
                badge = state.nextInstruction?.uppercase(),
                cue = null,
                emphasis = emphasisPriority
            )
            ReadingProfile.CHOIR -> DashboardPresentation(
                title = state.currentSection.uppercase(),
                subtitle = pagesUntil,
                badge = state.nextInstruction?.uppercase(),
                cue = null,
                emphasis = emphasisPriority
            )
            ReadingProfile.CONDUCTOR -> DashboardPresentation(
                title = String.format("%02d/%02d", state.repertoireProgress, state.repertoireTotal),
                subtitle = passShortText,
                badge = state.nextInstruction?.uppercase(),
                cue = null,
                emphasis = emphasisPriority
            )
            ReadingProfile.LITURGY -> DashboardPresentation(
                title = "MÚSICA ${String.format("%02d", state.repertoireProgress)}",
                subtitle = "TEMPO",
                badge = "${(state.elapsedTime / 60000)}m",
                cue = null,
                emphasis = emphasisPriority
            )
        }
    }
}
