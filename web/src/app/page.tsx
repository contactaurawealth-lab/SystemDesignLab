import React from "react";
import Navbar from "@/components/Navbar";
import HeroArchitecture from "@/components/HeroArchitecture";
import ScrollExpansion from "@/components/ScrollExpansion";
import ThreeModes from "@/components/ThreeModes";
import SimulationCenterpiece from "@/components/SimulationCenterpiece";
import LearningJourney from "@/components/LearningJourney";
import FailureLabPreview from "@/components/FailureLabPreview";
import ArchitecturePlaygroundPreview from "@/components/ArchitecturePlaygroundPreview";
import InterviewModePreview from "@/components/InterviewModePreview";
import AudiobookPreview from "@/components/AudiobookPreview";
import AiAssistantSection from "@/components/AiAssistantSection";
import ProgressSection from "@/components/ProgressSection";
import RepositoryExplorerPreview from "@/components/RepositoryExplorerPreview";
import OfflineSection from "@/components/OfflineSection";
import FinalCta from "@/components/FinalCta";
import Footer from "@/components/Footer";

export default function Home() {
  return (
    <main className="min-h-screen bg-white text-ink selection:bg-slate-200">
      <Navbar />
      <HeroArchitecture />
      <ScrollExpansion />
      <ThreeModes />
      <SimulationCenterpiece />
      <LearningJourney />
      <FailureLabPreview />
      <ArchitecturePlaygroundPreview />
      <InterviewModePreview />
      <AudiobookPreview />
      <AiAssistantSection />
      <ProgressSection />
      <RepositoryExplorerPreview />
      <OfflineSection />
      <FinalCta />
      <Footer />
    </main>
  );
}
