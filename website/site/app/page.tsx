import StickyNav from "./components/StickyNav";
import PortalSection from "./components/PortalSection";
import PlatformsSection from "./components/PlatformsSection";
import ThreatsSection from "./components/ThreatsSection";
import RocketsSection from "./components/RocketsSection";
import ArchiveSection from "./components/ArchiveSection";
import GallerySection from "./components/GallerySection";
import LaunchSection from "./components/LaunchSection";

export default function Home() {
  return (
    <div className="min-h-screen bg-black text-white selection:bg-cyan-500/30">
      <StickyNav />
      <main>
        <PortalSection />
        <PlatformsSection />
        <ThreatsSection />
        <RocketsSection />
        <ArchiveSection />
        <GallerySection />
        <LaunchSection />
      </main>
    </div>
  );
}
