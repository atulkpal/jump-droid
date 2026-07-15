"use client";

import { motion } from "framer-motion";
import { FEATURES } from "@/lib/constants";
import SectionWrapper from "./SectionWrapper";
import SectionHeader from "./SectionHeader";

export default function FeaturesSection() {
  return (
    <SectionWrapper id="features">
      <div className="absolute inset-0 bg-[radial-gradient(circle_at_top,_rgba(213,0,249,0.06),transparent_28%)]" />
      <div className="relative mx-auto max-w-6xl px-6 sm:px-8 lg:px-12">
        <SectionHeader
          pill="Features"
          title="What Awaits Above"
          description="Jump Droid combines precision platforming, resource management, and deep exploration into a single vertical ascent."
        />

        <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
          {FEATURES.map((feature, i) => (
            <motion.article
              key={feature.title}
              className="group rounded-3xl border border-cyan-300/10 bg-slate-950/60 p-6 backdrop-blur-md transition hover:border-cyan-400/40 hover:bg-slate-900/60"
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              viewport={{ once: true, margin: "-50px" }}
              transition={{ duration: 0.4, delay: i * 0.05 }}
            >
              <span className="text-2xl" role="img" aria-hidden="true">
                {feature.icon}
              </span>
              <h3 className="mt-4 text-lg font-bold tracking-wide text-white">
                {feature.title}
              </h3>
              <p className="mt-2 text-sm leading-relaxed text-slate-300">
                {feature.description}
              </p>
            </motion.article>
          ))}
        </div>
      </div>
    </SectionWrapper>
  );
}
