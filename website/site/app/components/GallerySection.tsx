import { SCREENSHOTS } from "@/lib/constants";

const SHOTS = SCREENSHOTS.slice(0, 4);

export default function GallerySection() {
  return (
    <section id="gallery" className="px-6 py-24 md:py-32">
      <div className="mx-auto max-w-lg">
        <p className="mb-2 text-xs font-bold uppercase tracking-[0.35em] text-slate-500">
          Gallery
        </p>
        <h2 className="mb-8 text-3xl font-black tracking-tight text-white md:text-4xl">
          See It in Action
        </h2>

        <div className="grid grid-cols-2 gap-3">
          {SHOTS.map((shot) => (
            <figure
              key={shot.src}
              className="rounded-xl overflow-hidden border border-white/10"
            >
              <img
                src={shot.src}
                alt={shot.alt}
                className="aspect-[9/16] w-full object-cover"
                loading="lazy"
              />
              <figcaption className="px-3 py-2 text-xs font-semibold text-slate-400">
                {shot.caption}
              </figcaption>
            </figure>
          ))}
        </div>
      </div>
    </section>
  );
}
