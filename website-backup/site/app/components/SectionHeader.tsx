type SectionHeaderProps = {
  pill: string;
  title: string;
  description?: string;
  pillClassName?: string;
};

export default function SectionHeader({
  pill,
  title,
  description,
  pillClassName = "text-cyan-300 border-cyan-400/20 bg-cyan-400/10",
}: SectionHeaderProps) {
  return (
    <div className="mb-12 max-w-2xl space-y-4">
      <p
        className={`inline-block rounded-full border px-3 py-1 text-sm font-extrabold uppercase tracking-[0.35em] ${pillClassName}`}
      >
        {pill}
      </p>
      <h2 className="text-4xl font-semibold tracking-tight text-white sm:text-5xl uppercase">
        {title}
      </h2>
      {description && (
        <p className="max-w-2xl text-sm leading-relaxed text-slate-300">
          {description}
        </p>
      )}
    </div>
  );
}
