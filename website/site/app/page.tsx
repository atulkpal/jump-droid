import StickyNav from "./components/StickyNav";
import PortalSection from "./components/PortalSection";
import GallerySection from "./components/GallerySection";
import LaunchSection from "./components/LaunchSection";

export default function Home() {
  return (
    <div className="min-h-screen bg-black text-white selection:bg-cyan-500/30">
      <StickyNav />
      <main>
        <PortalSection />
        <GallerySection />
        <LaunchSection />
      </main>
    </div>
  );
}
