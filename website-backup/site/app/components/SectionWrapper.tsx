type SectionWrapperProps = {
  children: React.ReactNode;
  id?: string;
  className?: string;
};

export default function SectionWrapper({ children, id, className = "" }: SectionWrapperProps) {
  return (
    <section
      id={id}
      className={`relative overflow-hidden py-24 sm:py-32 ${className}`}
    >
      {children}
    </section>
  );
}
