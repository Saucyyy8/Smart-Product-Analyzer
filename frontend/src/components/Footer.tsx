import { Github, Twitter, Linkedin, Zap } from "lucide-react";

const Footer = () => {
    return (
        <footer className="bg-muted/30 border-t border-border mt-auto">
            <div className="container mx-auto px-4 py-12">
                <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
                    <div className="col-span-1 md:col-span-2">
                        <div className="flex items-center gap-2 mb-4">
                            <Zap className="w-6 h-6 text-primary" />
                            <span className="text-xl font-bold">Smart Product Analyzer</span>
                        </div>
                        <p className="text-muted-foreground max-w-sm">
                            Making informed purchasing decisions easier with AI-powered analysis of products from around the web.
                        </p>
                    </div>

                    <div>
                        <h4 className="font-semibold mb-4">Product</h4>
                        <ul className="space-y-2 text-sm text-muted-foreground">
                            <li><a href="/analyze" className="hover:text-primary transition-colors">Start Analysis</a></li>
                            <li><a href="/dashboard" className="hover:text-primary transition-colors">History</a></li>
                            <li><a href="#" className="hover:text-primary transition-colors">Features</a></li>
                            <li><a href="#" className="hover:text-primary transition-colors">Pricing</a></li>
                        </ul>
                    </div>

                    <div>
                        <h4 className="font-semibold mb-4">Connect</h4>
                        <div className="flex gap-4">
                            <a href="#" className="text-muted-foreground hover:text-primary transition-colors">
                                <Github className="w-5 h-5" />
                            </a>
                            <a href="#" className="text-muted-foreground hover:text-primary transition-colors">
                                <Twitter className="w-5 h-5" />
                            </a>
                            <a href="#" className="text-muted-foreground hover:text-primary transition-colors">
                                <Linkedin className="w-5 h-5" />
                            </a>
                        </div>
                    </div>
                </div>

                <div className="border-t border-border pt-8 text-center text-sm text-muted-foreground">
                    <p>&copy; {new Date().getFullYear()} Smart Product Analyzer. All rights reserved.</p>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
