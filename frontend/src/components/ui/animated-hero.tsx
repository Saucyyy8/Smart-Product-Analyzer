import { useEffect, useMemo, useState } from "react";
import { motion } from "framer-motion";
import { MoveRight, PhoneCall, LayoutDashboard, Search } from "lucide-react";
import { Button } from "@/components/ui/button";
import { useNavigate } from "react-router-dom";

function Hero() {
    const [titleNumber, setTitleNumber] = useState(0);
    const titles = useMemo(
        () => ["smart", "fast", "reliable", "accurate", "detailed"],
        []
    );
    const navigate = useNavigate();

    useEffect(() => {
        const timeoutId = setTimeout(() => {
            if (titleNumber === titles.length - 1) {
                setTitleNumber(0);
            } else {
                setTitleNumber(titleNumber + 1);
            }
        }, 2000);
        return () => clearTimeout(timeoutId);
    }, [titleNumber, titles]);

    return (
        <div className="w-full relative overflow-hidden bg-background">
            <div className="absolute inset-0 bg-grid-pattern opacity-5" />
            <div className="container mx-auto px-4 relative z-10">
                <div className="flex gap-8 py-20 lg:py-40 items-center justify-center flex-col">
                    <div>
                        <Button variant="secondary" size="sm" className="gap-4">
                            New: Multi-Product Comparison <MoveRight className="w-4 h-4" />
                        </Button>
                    </div>
                    <div className="flex gap-4 flex-col text-center">
                        <h1 className="text-5xl md:text-7xl max-w-2xl tracking-tighter font-regular">
                            <span>Smart Analysis is</span>
                            <span className="relative flex w-full justify-center overflow-hidden text-center md:pb-4 md:pt-1">
                                &nbsp;
                                {titles.map((title, index) => (
                                    <motion.span
                                        key={index}
                                        className="absolute font-semibold text-primary"
                                        initial={{ opacity: 0, y: "-100" }}
                                        transition={{ type: "spring", stiffness: 50 }}
                                        animate={
                                            titleNumber === index
                                                ? {
                                                    y: 0,
                                                    opacity: 1,
                                                }
                                                : {
                                                    y: titleNumber > index ? -150 : 150,
                                                    opacity: 0,
                                                }
                                        }
                                    >
                                        {title}
                                    </motion.span>
                                ))}
                            </span>
                        </h1>

                        <p className="text-lg md:text-xl leading-relaxed tracking-tight text-muted-foreground max-w-2xl mx-auto">
                            Stop guessing which product to buy. Our AI analyzes reviews, specs, and prices to pinpoint the best choice for you in seconds.
                        </p>
                    </div>
                    <div className="flex flex-col sm:flex-row gap-4 mb-8">
                        <Button size="lg" className="gap-4 text-lg h-12 px-8 glow-primary" onClick={() => navigate("/analyze")}>
                            <Search className="w-5 h-5" /> Start Analyzing
                        </Button>
                        <Button size="lg" className="gap-4 h-12 px-8" variant="outline" onClick={() => navigate("/dashboard")}>
                            My Dashboard <LayoutDashboard className="w-5 h-5" />
                        </Button>
                    </div>
                </div>
            </div>
            {/* Simplified Gradient Orbs */}
            <div className="absolute top-20 left-10 w-32 h-32 bg-primary/20 rounded-full blur-3xl animate-pulse pointer-events-none" />
            <div className="absolute bottom-20 right-10 w-40 h-40 bg-accent/20 rounded-full blur-3xl animate-pulse delay-1000 pointer-events-none" />
        </div>
    );
}

export { Hero };
